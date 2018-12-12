package org.obsplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.taxmaster.data.TaxMappingRateData;
import org.obsplatform.finance.billingorder.api.BillingTransactionConstants;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ranjith
 *
 */
@Service
public class GenerateBill {

	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final ConfigurationRepository globalConfigurationRepository;
	private final PlanRepository planRepository;

	@Autowired
	public GenerateBill(final BillingOrderReadPlatformService billingOrderReadPlatformService,
			final ConfigurationRepository globalConfigurationRepository,final PlanRepository planRepository) {
		
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.globalConfigurationRepository = globalConfigurationRepository;
		this.planRepository = planRepository;
	}

	public boolean isChargeTypeNRC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("NRC")) {
			chargeType = true;
		}
		return chargeType;
	}

	public boolean isChargeTypeRC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("RC")) {
			chargeType = true;
		}
		return chargeType;
	}

	public boolean isChargeTypeUC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("UC")) {
			chargeType = true;
		}
		return chargeType;
	}

	// prorata monthly bill
	public BillingOrderCommand getProrataMonthlyFirstBill(final BillingOrderData billingOrderData,final DiscountMasterData discountMasterData) {

		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;
		
		LocalDate startDate = new LocalDate(billingOrderData.getBillStartDate());
		LocalDate monthStartDate = startDate.dayOfMonth().withMinimumValue();
		Plan plan = this.planRepository.findOne(billingOrderData.getPlanId());

		// check startDate is monthStartDate
		if (startDate.equals(monthStartDate)) {
			endDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);// durationDate
		} else {
			endDate = startDate.dayOfMonth().withMaximumValue();
		}

		if (endDate.toDate().before(billingOrderData.getBillEndDate()) || endDate.toDate().equals(billingOrderData.getBillEndDate())) {

			price = billingOrderData.getPrice();
			if (!startDate.equals(monthStartDate)) {
				price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),
						billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
			}
			// plan with No prorata and not start day of month
			if (plan.getBillRule() == 300 && startDate.compareTo(monthStartDate) > 0 && plan.isPrepaid() == 'N') {
				price = BigDecimal.ZERO;
			} else if(plan.getBillRule() == 200 && plan.isPrepaid() == 'N'){
				price =billingOrderData.getPrice();
			}

		} else if (endDate.toDate().after(billingOrderData.getBillEndDate())) {
			endDate = new LocalDate(billingOrderData.getBillEndDate());
			price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
		}
		
		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);

		// check promotion or discount is apply or not --Tax is calculated on
		// Net charges if those applied..
		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);
		
		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);
	}
	
	// NextMonth Bill after prorata
	public BillingOrderCommand getNextMonthBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		startDate = new LocalDate(billingOrderData.getNextBillableDate());
		endDate = new LocalDate(billingOrderData.getInvoiceTillDate()).plusMonths(billingOrderData.getChargeDuration()).dayOfMonth().withMaximumValue();
		
		if (endDate.toDate().before(billingOrderData.getBillEndDate()) || endDate.toDate().equals(billingOrderData.getBillEndDate())) {
			price = billingOrderData.getPrice();
			
		} else if (endDate.toDate().after(billingOrderData.getBillEndDate())) {
			
			endDate = new LocalDate(billingOrderData.getBillEndDate());
			price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
		}

		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);

		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);
		
		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);

	}

	// Monthly Bill
	public BillingOrderCommand getMonthlyBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		if (billingOrderData.getInvoiceTillDate() == null) {
			
			startDate = new LocalDate(billingOrderData.getBillStartDate());
			endDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
			price = billingOrderData.getPrice();
			
		} else if (billingOrderData.getInvoiceTillDate() != null) {
			startDate = new LocalDate(billingOrderData.getNextBillableDate());
			endDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
			
		}
		   if (endDate.toDate().before(billingOrderData.getBillEndDate()) || endDate.toDate().equals(billingOrderData.getBillEndDate())) {
				price = billingOrderData.getPrice();
			
			} else if (endDate.toDate().after(billingOrderData.getBillEndDate())) {
				
				endDate = new LocalDate(billingOrderData.getBillEndDate());
				price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
			}
		

		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);

		// check promotion or discount is apply or not --Tax is calculated on
		// Net charges if those applied..
		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);

	}

	// Pro rate Weekly Bill
	public BillingOrderCommand getProrataWeeklyFirstBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		startDate = new LocalDate(billingOrderData.getBillStartDate());
		endDate = startDate.dayOfWeek().withMaximumValue();
		LocalDate weekStartDate = startDate.dayOfWeek().withMinimumValue();
		Plan plan = this.planRepository.findOne(billingOrderData.getPlanId());

		int totalDays = 0;

		totalDays = Days.daysBetween(startDate, endDate).getDays() + 1;
		BigDecimal weeklyPricePerDay = getWeeklyPricePerDay(billingOrderData);

		Integer billingDays = 7 * billingOrderData.getChargeDuration();

		if (totalDays < billingDays) {
			price = weeklyPricePerDay.multiply(new BigDecimal(totalDays));
			if (plan.getBillRule() == 300 && !startDate.equals(weekStartDate)) {
				price = BigDecimal.ZERO;
			}
		} else if (totalDays == billingDays) {
			price = billingOrderData.getPrice();
		}

		invoiceTillDate = endDate;
		nextbillDate = endDate.plusDays(1);

		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);
		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,
				discountMasterData);

	}

	public BillingOrderCommand getNextWeeklyBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		startDate = new LocalDate(billingOrderData.getNextBillableDate());
		endDate = startDate.plusWeeks(billingOrderData.getChargeDuration()).minusDays(1);

		if (endDate.toDate().before(billingOrderData.getBillEndDate()) || 
				endDate.toDate().compareTo(billingOrderData.getBillEndDate()) == 0) {
			price = billingOrderData.getPrice();
		} else if (endDate.toDate().after(billingOrderData.getBillEndDate())) {
			endDate = new LocalDate(billingOrderData.getBillEndDate());
			price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),
					billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
		}

		invoiceTillDate = endDate;
		nextbillDate = endDate.plusDays(1);

		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);
		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, 
				price, listOfTaxes,discountMasterData);

	}

	// Weekly Bill
	public BillingOrderCommand getWeeklyBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		if (billingOrderData.getInvoiceTillDate() == null) {
			
			startDate = new LocalDate(billingOrderData.getBillStartDate());
			endDate = startDate.plusWeeks(billingOrderData.getChargeDuration()).minusDays(1);
			price = billingOrderData.getPrice();
		
		} else if (billingOrderData.getInvoiceTillDate() != null) {
			startDate = new LocalDate(billingOrderData.getNextBillableDate());
			endDate = startDate.plusWeeks(billingOrderData.getChargeDuration()).minusDays(1);
			
			if (endDate.toDate().before(billingOrderData.getBillEndDate()) || endDate.toDate().compareTo(billingOrderData.getBillEndDate()) == 0) {
				price = billingOrderData.getPrice();
			
			} else if (endDate.toDate().after(billingOrderData.getBillEndDate())) {
				
				endDate = new LocalDate(billingOrderData.getBillEndDate());
				price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),billingOrderData.getDurationType(),
						billingOrderData.getChargeDuration());
			}
		}

		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);
		List<InvoiceTaxCommand> listOfTaxes=this.calculateDiscountAndTax(billingOrderData,discountMasterData,startDate,endDate,price);

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,
				discountMasterData);
	}

	// One Time Bill
	public BillingOrderCommand getOneTimeBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate endDate=null;
		LocalDate invoiceTillDate=null;
		LocalDate nextbillDate=null;
		List<InvoiceTaxCommand> listOfTaxes = new ArrayList<InvoiceTaxCommand>();

		LocalDate startDate = new LocalDate(billingOrderData.getBillStartDate());
		BigDecimal price = billingOrderData.getPrice();
		endDate = startDate;
	    invoiceTillDate = startDate;
	    nextbillDate = invoiceTillDate.plusDays(1);

		if(discountMasterData !=null && BigDecimal.ZERO.compareTo(discountMasterData.getDiscountAmount()) <= 1){

			listOfTaxes = this.calculateTax(billingOrderData,discountMasterData.getDiscountedChargeAmount());
		} else {
			
			listOfTaxes = this.calculateTax(billingOrderData,billingOrderData.getPrice());
		}

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate,
				nextbillDate, price, listOfTaxes,discountMasterData);
	}

	// order last recurring price
	protected BigDecimal getLastRecurringPrice(LocalDate startDate,LocalDate endDate, BigDecimal amount, String durationType,
			Integer chargeDuration) {

		LocalDate durationDate = startDate.plusMonths(chargeDuration).minusDays(1);
		int divisibleDays = Days.daysBetween(startDate, durationDate).getDays() + 1;
		int maxDaysOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
		int maximumDaysInYear = new LocalDate().dayOfYear().withMaximumValue().getDayOfYear();
		BigDecimal pricePerDay = BigDecimal.ZERO;

		int totalDays =  Days.daysBetween(startDate, endDate).getDays() + 1;
		
		if (durationType.equalsIgnoreCase("month(s)")) {

			if (chargeDuration == 12) {
				pricePerDay = amount.divide(new BigDecimal(maximumDaysInYear),RoundingMode.HALF_UP);

			} else if(chargeDuration != 1){
				pricePerDay = amount.divide(new BigDecimal(divisibleDays),RoundingMode.HALF_UP);

			} else {
				pricePerDay = amount.divide(new BigDecimal(maxDaysOfMonth),RoundingMode.HALF_UP);
			}
		} else if (durationType.equalsIgnoreCase("week(s)")) {

			Integer billingDays = 7 * chargeDuration;

			pricePerDay = amount.divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
	}
		

		return pricePerDay.multiply(new BigDecimal(totalDays));

	}

	// order cancelled bill
	public BillingOrderCommand getCancelledOrderBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {
		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;
		if (billingOrderData.getInvoiceTillDate() == null)
			startDate = new LocalDate(billingOrderData.getStartDate());
		else
			startDate = new LocalDate(billingOrderData.getNextBillableDate());

		endDate = new LocalDate(billingOrderData.getBillEndDate());
		price = this.getLastRecurringPrice(startDate, endDate,billingOrderData.getPrice(),billingOrderData.getDurationType(), null);

		nextbillDate = new LocalDate().plusYears(1000);
		invoiceTillDate = endDate;
		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(billingOrderData, price);

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);

	}

	// Per day weekly price
	public BigDecimal getWeeklyPricePerDay(BillingOrderData billingOrderData) {
		Integer billingDays = 7 * billingOrderData.getChargeDuration();

		return billingOrderData.getPrice().divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
	}

	// Daily Bill
	public BillingOrderCommand getDailyBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		LocalDate startDate = null;
		LocalDate endDate = null;
		BigDecimal price = null;
		LocalDate invoiceTillDate = null;
		LocalDate nextbillDate = null;

		if (billingOrderData.getNextBillableDate() == null) {
			startDate = new LocalDate(billingOrderData.getBillStartDate());
			endDate = startDate;

		} else {
			startDate = new LocalDate(billingOrderData.getNextBillableDate());
			endDate = startDate;
		}

		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);
		price = billingOrderData.getPrice();
		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(billingOrderData, price);
		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);

	}
	
	public List<InvoiceTaxCommand> calculateDiscountAndTax(BillingOrderData billingOrderData,DiscountMasterData discountMasterData, LocalDate startDate,
			LocalDate endDate, BigDecimal price) {

		List<InvoiceTaxCommand> listOfTaxes = new ArrayList<>();

		if (this.isDiscountApplicable(startDate, discountMasterData, endDate)) {

			discountMasterData = this.calculateDiscount(discountMasterData,price);

			// Tax is calculated on netChages..whenever customer has tax exemption false and discount applicabled
			if (!billingOrderData.isTaxExemption())
				listOfTaxes = this.calculateTax(billingOrderData,discountMasterData.getDiscountedChargeAmount());

		} else if (this.isDiscountAtMiddleOfMonth(startDate,discountMasterData, endDate, billingOrderData)) {

			/*BigDecimal promoPrice = this.getLastRecurringPrice(discountMasterData.getDiscountEndDate().plusDays(1),
					endDate, price, billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
			*/
			BigDecimal promoPrice = this.getLastRecurringPrice(startDate,discountMasterData.getDiscountEndDate(),
					 price, billingOrderData.getDurationType(),billingOrderData.getChargeDuration());
			
			
			/* discountMasterData.setDiscountAmount(price.subtract(promoPrice).setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP));
			 discountMasterData.setDiscountedChargeAmount(promoPrice.setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP)); */

			//for discount calculation of middile of month
			BigDecimal discountAmount = promoPrice.multiply(discountMasterData.getDiscountRate().divide(new BigDecimal(100))).setScale(Integer.parseInt(roundingDecimal()), RoundingMode.HALF_UP);
			BigDecimal discountedPromoPrice =promoPrice.subtract(discountAmount).setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP);
			
			discountMasterData.setDiscountAmount(discountAmount.setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP));
			 discountMasterData.setDiscountedChargeAmount(discountedPromoPrice.setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP));

			
			if (!billingOrderData.isTaxExemption())
				listOfTaxes = this.calculateTax(billingOrderData, promoPrice);

		} else {
			// Tax is calculated on charges ..whenever customer has tax exemption false
			if (!billingOrderData.isTaxExemption())
				listOfTaxes = this.calculateTax(billingOrderData, price);
		}

		return listOfTaxes;
	}

	// Tax Calculation
	public List<InvoiceTaxCommand> calculateTax(BillingOrderData billingOrderData, BigDecimal billPrice) {

		// Get State level taxes
		List<TaxMappingRateData> taxMappingRateDatas = billingOrderReadPlatformService.retrieveTaxMappingData(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		if (taxMappingRateDatas.isEmpty()) {
				taxMappingRateDatas = billingOrderReadPlatformService.retrieveDefaultTaxMappingData(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		}
		
		List<InvoiceTaxCommand> invoiceTaxCommand = generateInvoiceTax(taxMappingRateDatas, billPrice,billingOrderData.getClientId(),billingOrderData.getTaxInclusive());
		return invoiceTaxCommand;
	}

	// Generate Invoice Tax
	public List<InvoiceTaxCommand> generateInvoiceTax(List<TaxMappingRateData> taxMappingRateDatas, BigDecimal price,Long clientId,Integer isTaxInclusive) {

		BigDecimal taxRate = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		String taxCode = null;
		
		List<InvoiceTaxCommand> invoiceTaxCommands = new ArrayList<InvoiceTaxCommand>();
		InvoiceTaxCommand invoiceTaxCommand = null;

		if (taxMappingRateDatas != null && !taxMappingRateDatas.isEmpty()) {

			for (TaxMappingRateData taxMappingRateData : taxMappingRateDatas) {

				if ("Percentage".equalsIgnoreCase(taxMappingRateData.getTaxType())) {
					
					taxRate = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					  if(isTaxInclusive.compareTo(Integer.valueOf(1))==0){  /*(2990 * 11) / (100 + 11)*/
                      	   taxAmount= price.multiply(taxRate).divide(new BigDecimal(100).add(taxRate),Integer.parseInt(roundingDecimal()), RoundingMode.HALF_UP);
                       }else{
					       taxAmount = price.multiply(taxRate.divide(new BigDecimal(100))).setScale(Integer.parseInt(roundingDecimal()), RoundingMode.HALF_UP);
                    }
				} else if ("Flat".equalsIgnoreCase(taxMappingRateData.getTaxType())) {
					
					taxRate = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					// taxAmount =taxFlat;
					if (price.compareTo(taxRate) < 0) {
						taxAmount = BigDecimal.ZERO;
					} else {
						taxAmount = taxRate;
					}
				}

				invoiceTaxCommand = new InvoiceTaxCommand(clientId, null, null,taxCode, isTaxInclusive, taxRate, taxAmount,price);
				invoiceTaxCommands.add(invoiceTaxCommand);
			}

		}else{
			invoiceTaxCommand = new InvoiceTaxCommand(clientId, null, null,taxCode, isTaxInclusive, taxRate, taxAmount,price);
			invoiceTaxCommands.add(invoiceTaxCommand);
		}
		return invoiceTaxCommands;

	}

	// Discount Applicable Logic
	public Boolean isDiscountApplicable(LocalDate chargeStartDate,DiscountMasterData discountMasterData, LocalDate chargeEndDate) {
		
		boolean isDiscountApplicable = false;

		if (discountMasterData != null) {
			if ((chargeStartDate.toDate().after(discountMasterData.getDiscountStartDate().toDate()) 
					|| chargeStartDate.toDate().equals(discountMasterData.getDiscountStartDate().toDate()))
					&& (chargeEndDate.toDate().before(this.getDiscountEndDateIfNull(discountMasterData,chargeEndDate)) 	
					||	chargeEndDate.toDate().equals(this.getDiscountEndDateIfNull(discountMasterData,chargeEndDate)))) {

				isDiscountApplicable = true;
			}
		}

		return isDiscountApplicable;

	}

	// Discount End Date calculation if null
	public Date getDiscountEndDateIfNull(DiscountMasterData discountMasterData,LocalDate chargeEndDate) {
		LocalDate discountEndDate = discountMasterData.getDiscountEndDate();
		if (discountMasterData.getDiscountEndDate() == null) {
			discountEndDate = chargeEndDate;
		}
		return discountEndDate.toDate();

	}

	// if is percentage
	public boolean isDiscountPercentage(DiscountMasterData discountMasterData) {
		boolean isDiscountPercentage = false;
		if ("percentage".equalsIgnoreCase(discountMasterData.getDiscountType())) {
			isDiscountPercentage = true;
		}
		return isDiscountPercentage;
	}

	// if is flat
	public boolean isDiscountFlat(DiscountMasterData discountMasterData) {
		boolean isDiscountFlat = false;
		if ("Flat".equalsIgnoreCase(discountMasterData.getDiscountType())) {
			isDiscountFlat = true;
		}
		return isDiscountFlat;
	}

	// Discount calculation
	public DiscountMasterData calculateDiscount(DiscountMasterData discountMasterData,BigDecimal chargePrice) {
		
		BigDecimal discountAmount=BigDecimal.ZERO;

		if (isDiscountPercentage(discountMasterData)) {

			discountAmount = this.calculateDiscountPercentage(discountMasterData.getDiscountRate(), chargePrice);
			chargePrice = this.chargePriceNotLessThanZero(chargePrice,discountAmount);
			discountMasterData.setDiscountAmount(discountAmount);
			discountMasterData.setDiscountedChargeAmount(chargePrice);

		}

		if (isDiscountFlat(discountMasterData)) {

			BigDecimal netFlatAmount = this.calculateDiscountFlat(discountMasterData.getDiscountRate(), chargePrice);
			discountAmount = chargePrice.subtract(netFlatAmount).setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP);
			netFlatAmount = this.chargePriceNotLessThanZero(chargePrice,discountAmount);
			discountMasterData.setDiscountAmount(discountAmount);
			discountMasterData.setDiscountedChargeAmount(netFlatAmount);
		}
		return discountMasterData;

	}

	// Dicount Percent calculation
	public BigDecimal calculateDiscountPercentage(BigDecimal discountRate,BigDecimal chargePrice) {

		return chargePrice.multiply(discountRate.divide(new BigDecimal(100))).setScale(Integer.parseInt(roundingDecimal()), RoundingMode.HALF_UP);
	}

	// Discount Flat calculation
	public BigDecimal calculateDiscountFlat(BigDecimal discountRate,BigDecimal chargePrice) {

		BigDecimal calculateDiscountFlat = BigDecimal.ZERO;
		// if chargePrice is Zero then discount is Zero
		if (chargePrice.compareTo(BigDecimal.ZERO) == 1) {
			calculateDiscountFlat = chargePrice.subtract(discountRate);
		}
		return calculateDiscountFlat;

	}

	// To check price not less than zero
	public BigDecimal chargePriceNotLessThanZero(BigDecimal chargePrice,BigDecimal discountPrice) {

		chargePrice = chargePrice.subtract(discountPrice);
		if (chargePrice.compareTo(BigDecimal.ZERO) < 0) {
			chargePrice = BigDecimal.ZERO;
		}
		return chargePrice;
	}

	// create billing order command
	public BillingOrderCommand createBillingOrderCommand(BillingOrderData billingOrderData, LocalDate chargeStartDate,LocalDate chargeEndDate, LocalDate invoiceTillDate,
			LocalDate nextBillableDate, BigDecimal billPrice,List<InvoiceTaxCommand> listOfTaxes,DiscountMasterData discountMasterData) {
		         
		      BigDecimal price = billPrice.setScale(Integer.parseInt(roundingDecimal()),RoundingMode.HALF_UP);
		      List<UsageChargesData> usageChargeDatas = new ArrayList<UsageChargesData>();
		      
		 return new BillingOrderCommand(billingOrderData.getClientOrderId(),billingOrderData.getOderPriceId(),
				billingOrderData.getClientId(), chargeStartDate.toDate(),nextBillableDate.toDate(), chargeEndDate.toDate(),
				billingOrderData.getBillingFrequency(),billingOrderData.getChargeCode(),billingOrderData.getChargeType(),
				billingOrderData.getChargeDuration(),billingOrderData.getDurationType(), invoiceTillDate.toDate(),
				price, billingOrderData.getBillingAlign(), listOfTaxes,billingOrderData.getStartDate(), billingOrderData.getEndDate(),
				discountMasterData, billingOrderData.getTaxInclusive(),BillingTransactionConstants.INVOICE,usageChargeDatas);
	}

	
	//rounding amount
	public String roundingDecimal() {

		final Configuration property = this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_ROUNDING);
		
		if(property != null && property.isEnabled()){
		
		  return property.getValue();
		}else{ 
			return String.valueOf(2);
		}
		
	}
	

	public boolean isDiscountAtMiddleOfMonth(LocalDate chargeStartDate,DiscountMasterData discountMasterData, 
			LocalDate chargeEndDate,BillingOrderData billingOrderData) {

		boolean isPromotionAtMiddleOfMonth = false;

		if (discountMasterData != null && discountMasterData.getDiscountEndDate() != null) {

			if (chargeEndDate.getYear() == discountMasterData.getDiscountEndDate().getYear() && 
					chargeEndDate.getMonthOfYear() == discountMasterData.getDiscountEndDate().getMonthOfYear() &&
					(chargeStartDate.toDate().before(discountMasterData.getDiscountEndDate().toDate()) 
							 || chargeStartDate.toDate().equals(discountMasterData.getDiscountEndDate().toDate()))	) {
				isPromotionAtMiddleOfMonth = true;
			}
		}

		return isPromotionAtMiddleOfMonth;
	}

}
