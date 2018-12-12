package org.obsplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.taxmaster.data.TaxMappingRateData;
import org.obsplatform.finance.billingorder.api.BillingTransactionConstants;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ranjith
 *
 */
@Service
public class GenerateDisconnectionBill {

	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateBill generateBill;
	
	BigDecimal pricePerMonth=BigDecimal.ZERO;
	BigDecimal price=BigDecimal.ZERO;
	LocalDate startDate=null;
	LocalDate endDate=null;
	LocalDate billEndDate=null;
	LocalDate invoiceTillDate=null;
	LocalDate nextbillDate=null;
	

	@Autowired
	public GenerateDisconnectionBill(final BillingOrderReadPlatformService billingOrderReadPlatformService,final GenerateBill generateBill) {
		
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateBill = generateBill;
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

	// Monthly Bill
	public BillingOrderCommand getReverseMonthyBill(final BillingOrderData billingOrderData,final DiscountMasterData discountMasterData,
			final LocalDate disconnectionDate) {

		BigDecimal discountAmount = BigDecimal.ZERO;
		BigDecimal netAmount = BigDecimal.ZERO;
		List<InvoiceTaxCommand> listOfTaxes = new ArrayList<InvoiceTaxCommand>();
		int numberOfDays = 0;
		int totalDays = 0;

		billEndDate = new LocalDate(billingOrderData.getBillEndDate());
		price = billingOrderData.getPrice();
		
		// with out invoice direct disconnect order
		if (billingOrderData.getInvoiceTillDate() == null) { 

			System.out.println("---- RC ----");
			this.startDate = new LocalDate(billingOrderData.getBillStartDate());
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			LocalDate nextDurationDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
			numberOfDays = Days.daysBetween(startDate, invoiceTillDate).getDays() + 1;

			if (billingOrderData.getChargeDuration() == 12) {
				int maxDaysInYear = startDate.dayOfYear().withMaximumValue().getDayOfYear();
				netAmount = price.divide(new BigDecimal(maxDaysInYear),RoundingMode.HALF_UP);
			} else if (billingOrderData.getChargeDuration() != 1) {
				totalDays = Days.daysBetween(startDate, nextDurationDate).getDays() + 1;
				netAmount = price.divide(new BigDecimal(totalDays),RoundingMode.HALF_UP);
			} else {
				int maxDaysOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
				netAmount = price.divide(new BigDecimal(maxDaysOfMonth),RoundingMode.HALF_UP);
			}
			price = netAmount.multiply(new BigDecimal(numberOfDays));
			
			listOfTaxes = this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData, startDate, endDate, price);
		}  
		// disconnect order With future date which is after order invoiceDate
		else if(disconnectionDate.toDate().after(billingOrderData.getInvoiceTillDate())) {
			
				System.out.println("---- RC ----");
				this.startDate = new LocalDate(billingOrderData.getNextBillableDate());
				this.endDate = disconnectionDate;
				this.invoiceTillDate = disconnectionDate;
				this.nextbillDate = invoiceTillDate.plusDays(1);
				LocalDate nextDurationDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
				numberOfDays = Days.daysBetween(startDate, invoiceTillDate).getDays() + 1;

				if (billingOrderData.getChargeDuration() == 12) {
					int maxDaysInYear = startDate.dayOfYear().withMaximumValue().getDayOfYear();
					netAmount = price.divide(new BigDecimal(maxDaysInYear),RoundingMode.HALF_UP);
				} else if (billingOrderData.getChargeDuration() != 1) {
					totalDays = Days.daysBetween(startDate, nextDurationDate).getDays() + 1;
					netAmount = price.divide(new BigDecimal(totalDays),RoundingMode.HALF_UP);
				} else {
					int maxDaysOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
					netAmount = price.divide(new BigDecimal(maxDaysOfMonth),RoundingMode.HALF_UP);
				}
				price = netAmount.multiply(new BigDecimal(numberOfDays));
				
				listOfTaxes = this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData, startDate, endDate, price);
		}
		// If invoice till date not null or after invoice disconnect order
		else { 

			System.out.println("---- DC ----");
			if (discountMasterData != null && BigDecimal.ZERO.compareTo(discountMasterData.getDiscountRate()) < 0) {

				if (this.IsDiscountOrPromotionApplicable(billingOrderData,discountMasterData)) {

					if ("percentage".equalsIgnoreCase(discountMasterData.getDiscountType())) {
						discountAmount = price.multiply(discountMasterData.getDiscountRate().divide(new BigDecimal(100),RoundingMode.HALF_UP));
						price = price.subtract(discountAmount);
					} else if ("flat".equalsIgnoreCase(discountMasterData.getDiscountType())) {
						price = price.subtract(discountMasterData.getDiscountRate());
					}
					
					price = this.calcualateMonthlyDisconnectionCredit(billingOrderData,disconnectionDate);
					
				}else if(this.IsDiscountOrPromotionEndedBeforeOrderInvoice(billingOrderData, discountMasterData)){
					
					if(disconnectionDate.toDate().before(discountMasterData.getDiscountEndDate().toDate())){
						invoiceTillDate = new LocalDate(billingOrderData.getInvoiceTillDate());
						int maxDaysOfMonth = disconnectionDate.dayOfMonth().withMaximumValue().getDayOfMonth();
						numberOfDays = Days.daysBetween(discountMasterData.getDiscountEndDate(),invoiceTillDate).getDays();
						price = ((price.divide(new BigDecimal(maxDaysOfMonth),RoundingMode.HALF_UP))
								   .multiply(new BigDecimal(numberOfDays))).setScale(2,RoundingMode.HALF_UP);
					}else{
						price = this.calcualateMonthlyDisconnectionCredit(billingOrderData,disconnectionDate);
					}
			   }

			}
			else{
				
				price = this.calcualateMonthlyDisconnectionCredit(billingOrderData,disconnectionDate);
			}
			
			if (BigDecimal.ZERO.compareTo(price) != 0) {
				listOfTaxes = this.calculateTax(billingOrderData, price,disconnectionDate);
			}

			this.startDate = invoiceTillDate;
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			billingOrderData.setChargeType("DC");
				
		}

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, 
				   nextbillDate, price, listOfTaxes,discountMasterData);
	}


	// Reverse Weekly Bill
	public BillingOrderCommand getReverseWeeklyBill(final BillingOrderData billingOrderData,final DiscountMasterData discountMasterData,
			final LocalDate disconnectionDate) {

		BigDecimal discountAmount = BigDecimal.ZERO;
		BigDecimal netAmount = BigDecimal.ZERO;
		int numberOfDays = 0;
		List<InvoiceTaxCommand> listOfTaxes=new ArrayList<InvoiceTaxCommand>();

		billEndDate = new LocalDate(billingOrderData.getBillEndDate());
		price = billingOrderData.getPrice();

		// with out invoice direct disconnect order
		if (billingOrderData.getInvoiceTillDate() == null) {

			this.startDate = new LocalDate(billingOrderData.getBillStartDate());
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			Integer billingDays = 7 * billingOrderData.getChargeDuration();
			numberOfDays = Days.daysBetween(startDate, nextbillDate).getDays();
			netAmount = price.divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
			price = netAmount.multiply(new BigDecimal(numberOfDays));
			listOfTaxes = this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData,startDate,endDate,price);

		} 
		// disconnect order With future date which is after order invoiceDate
		else if (disconnectionDate.toDate().after(billingOrderData.getInvoiceTillDate())) {

			this.startDate = new LocalDate(billingOrderData.getNextBillableDate());
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			Integer billingDays = 7 * billingOrderData.getChargeDuration();
			numberOfDays = Days.daysBetween(startDate, nextbillDate).getDays();
			netAmount = price.divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
			price = netAmount.multiply(new BigDecimal(numberOfDays));
			listOfTaxes = this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData,startDate, endDate,price);
		}
		// if invoice till date not null or after invoice disconnect order
		else {

			if (discountMasterData != null&& BigDecimal.ZERO.compareTo(discountMasterData.getDiscountRate()) < 0) {

				if (this.IsDiscountOrPromotionApplicable(billingOrderData,discountMasterData)) {

					if ("percentage".equalsIgnoreCase(discountMasterData.getDiscountType())) {
						discountAmount = price.multiply(discountMasterData.getDiscountRate().divide(new BigDecimal(100),RoundingMode.HALF_UP));
						price = price.subtract(discountAmount);
					} else if ("flat".equalsIgnoreCase(discountMasterData.getDiscountType())) {
						price = price.subtract(discountMasterData.getDiscountRate());
					}
					price = this.calcualateWeeklyDisconnectionCredit(billingOrderData,disconnectionDate);
					
				}else if(this.IsDiscountOrPromotionEndedBeforeOrderInvoice(billingOrderData, discountMasterData)){
					
				//Here not write logic
				
				}
			}
			else{
				
				price = this.calcualateWeeklyDisconnectionCredit(billingOrderData,disconnectionDate);
			}

			if (BigDecimal.ZERO.compareTo(price) != 0) {
				listOfTaxes = this.calculateTax(billingOrderData, price,disconnectionDate);
			}
			
			this.startDate = invoiceTillDate;
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			billingOrderData.setChargeType("DC");
		}

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate,
				nextbillDate, price, listOfTaxes,discountMasterData);
	}

	// Per day weekly price
	public BigDecimal getWeeklyPricePerDay(Integer chargeDuration,BigDecimal price) {
		Integer billingDays = 7 * chargeDuration; 
		return price.divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
	}

	// Daily Bill
	public BillingOrderCommand getDailyBill(final BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {

		startDate = new LocalDate(billingOrderData.getBillStartDate());
		endDate = startDate;
		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);
		price = billingOrderData.getPrice();

		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(billingOrderData, price,null);

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate, invoiceTillDate, 
				nextbillDate, price, listOfTaxes,discountMasterData);

	}
	
	// Reverse One Time Bill
	public BillingOrderCommand getReverseOneTimeBill(BillingOrderData billingOrderData,DiscountMasterData discountMasterData) {
            
		  List<InvoiceTaxCommand> listOfTaxes = new ArrayList<InvoiceTaxCommand>();
			LocalDate startDate = new LocalDate(billingOrderData.getBillStartDate());
			LocalDate  endDate = startDate;
			LocalDate  invoiceTillDate = startDate;
			LocalDate   nextbillDate = invoiceTillDate;
			BigDecimal price = billingOrderData.getPrice();//totalPrice
			  if(discountMasterData !=null && BigDecimal.ZERO.compareTo(discountMasterData.getDiscountAmount()) < 0){	
				   BigDecimal discountedPrice = price.subtract(discountMasterData.getDiscountAmount());
				   listOfTaxes = this.calculateTax(billingOrderData,discountedPrice,startDate);
		        }else{
		           listOfTaxes = this.calculateTax(billingOrderData,price,startDate);
		        }

		return this.createBillingOrderCommand(billingOrderData, startDate,endDate,invoiceTillDate,nextbillDate,price,
					listOfTaxes,discountMasterData);
		}
	

	// Tax Calculation
	public List<InvoiceTaxCommand> calculateTax(final BillingOrderData billingOrderData, final BigDecimal billPrice, LocalDate disconnectionDate) {

		List<TaxMappingRateData> taxMappingRateDatas = billingOrderReadPlatformService.retrieveTaxMappingData(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		if(taxMappingRateDatas.isEmpty()){
			
			 taxMappingRateDatas = billingOrderReadPlatformService.retrieveDefaultTaxMappingData(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		}
		List<InvoiceTaxCommand> invoiceTaxCommand = this.generateInvoiceTax(taxMappingRateDatas, billPrice, billingOrderData,disconnectionDate);
		return invoiceTaxCommand;
	}
	
	// Generate Invoice Tax
	public List<InvoiceTaxCommand> generateInvoiceTax(final List<TaxMappingRateData> taxMappingRateDatas, final BigDecimal price,
			BillingOrderData billingOrderData,LocalDate disconnectionDate) {
		
		   BigDecimal taxRate= null;
		   BigDecimal taxAmount = null;
		   String taxCode = null;
		   
		 List<InvoiceTaxCommand> invoiceTaxCommands = new ArrayList<InvoiceTaxCommand>();
		 InvoiceTaxCommand invoiceTaxCommand = null;
		 
		if (taxMappingRateDatas != null) {

			for (TaxMappingRateData taxMappingRateData : taxMappingRateDatas) {

				if ("Percentage".equalsIgnoreCase(taxMappingRateData.getTaxType())) {
					
					taxRate = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					 if(billingOrderData.getTaxInclusive().compareTo(Integer.valueOf(1))==0){  /*(2990 * 11) / (100 + 11)*/
	                      	 taxAmount=price.multiply(taxRate).divide(new BigDecimal(100).add(taxRate),Integer.parseInt(this.generateBill.roundingDecimal()), RoundingMode.HALF_UP);
	                     }else{
	                    	 taxAmount = price.multiply(taxRate.divide(new BigDecimal(100))).setScale(Integer.parseInt(this.generateBill.roundingDecimal()),RoundingMode.HALF_UP);
	                    }
				} else if("Flat".equalsIgnoreCase(taxMappingRateData.getTaxType())) {
					
					taxRate = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					if("RC".equalsIgnoreCase(billingOrderData.getChargeType())||"NRC".equalsIgnoreCase(billingOrderData.getChargeType())){
					      taxAmount =taxRate;
					}else{
					BigDecimal numberOfMonthsPrice = BigDecimal.ZERO;
					BigDecimal numberOfDaysPrice = BigDecimal.ZERO;
					BigDecimal pricePerDay = BigDecimal.ZERO;
					BigDecimal pricePerMonth = BigDecimal.ZERO;
					LocalDate durationDate=disconnectionDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
					int totalDays = Days.daysBetween(disconnectionDate, durationDate).getDays();
				    int numberOfMonths = Months.monthsBetween(disconnectionDate,new LocalDate(billingOrderData.getInvoiceTillDate())).getMonths();
				    int maximumDaysInMonth = disconnectionDate.dayOfMonth().withMaximumValue().getDayOfMonth();
			        int  maximumDaysInYear = new LocalDate().dayOfYear().withMaximumValue().getDayOfYear();
					int numberOfDays = 0;
					if(numberOfMonths !=0){
						LocalDate tempDate = new LocalDate(billingOrderData.getInvoiceTillDate()).minusMonths(numberOfMonths);
						numberOfDays = Days.daysBetween(disconnectionDate, tempDate).getDays();	
					}else{
						numberOfDays = Days.daysBetween(disconnectionDate,new LocalDate(billingOrderData.getInvoiceTillDate())).getDays();
					}
					if(billingOrderData.getDurationType().equalsIgnoreCase("month(s)")){
					     if(billingOrderData.getChargeDuration()==12){
					    	 
						       pricePerMonth = taxRate.divide(new BigDecimal(billingOrderData.getChargeDuration()),RoundingMode.HALF_UP);
					           numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxRate.divide(new BigDecimal(maximumDaysInYear),RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				    	       
				      }else if(billingOrderData.getChargeDuration()!=1){
				    	  
						       pricePerMonth = taxRate.divide(new BigDecimal(billingOrderData.getChargeDuration()),RoundingMode.HALF_UP);
						       numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxRate.divide(new BigDecimal(totalDays),RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      }else{
				    	       numberOfMonthsPrice = taxRate.multiply(new BigDecimal(numberOfMonths));
						       pricePerDay = taxRate.divide(new BigDecimal(maximumDaysInMonth),RoundingMode.HALF_UP);
						       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				       }
					} else if(billingOrderData.getDurationType().equalsIgnoreCase("week(s)")){ 
						
							  Integer billingDays = 7*billingOrderData.getChargeDuration();
				    	      pricePerDay = taxRate.divide(new BigDecimal(billingDays),RoundingMode.HALF_UP);
				    	      numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      
					}  
					taxAmount = numberOfDaysPrice.add(numberOfMonthsPrice).setScale(Integer.parseInt(this.generateBill.roundingDecimal()),RoundingMode.HALF_UP);
					// taxAmount = taxFlat;
				}
				}
			
				invoiceTaxCommand = new InvoiceTaxCommand(billingOrderData.getClientId(), null, null,
						                  taxCode, billingOrderData.getTaxInclusive(), taxRate, taxAmount,price);
				invoiceTaxCommands.add(invoiceTaxCommand);
			}

	    }
		return invoiceTaxCommands;

	}

	// create billing order command
	public BillingOrderCommand createBillingOrderCommand(BillingOrderData billingOrderData, LocalDate chargeStartDate,
			LocalDate chargeEndDate, LocalDate invoiceTillDate,LocalDate nextBillableDate, BigDecimal billPrice,
			List<InvoiceTaxCommand> listOfTaxes,DiscountMasterData discountMasterData) {

		 BigDecimal price = billPrice.setScale(Integer.parseInt(this.generateBill.roundingDecimal()),RoundingMode.HALF_UP);
		 List<UsageChargesData> usageChargeDatas = new ArrayList<UsageChargesData>();
		
		return new BillingOrderCommand(billingOrderData.getClientOrderId(),
				billingOrderData.getOderPriceId(),
				billingOrderData.getClientId(), chargeStartDate.toDate(),
				nextBillableDate.toDate(), chargeEndDate.toDate(),
				billingOrderData.getBillingFrequency(),
				billingOrderData.getChargeCode(),
				billingOrderData.getChargeType(),
				billingOrderData.getChargeDuration(),
				billingOrderData.getDurationType(), invoiceTillDate.toDate(),
				price, billingOrderData.getBillingAlign(), listOfTaxes,
				billingOrderData.getStartDate(), billingOrderData.getEndDate(),
				discountMasterData, billingOrderData.getTaxInclusive(),
				BillingTransactionConstants.INVOICE,usageChargeDatas);
	}
	
	//  Calcualate Monthly DisconnectionCredit  
	public BigDecimal calcualateMonthlyDisconnectionCredit(BillingOrderData billingOrderData, LocalDate disconnectionDate) {

		BigDecimal disconnectionCreditForMonths = BigDecimal.ZERO;
		BigDecimal disconnectionCreditPerday = BigDecimal.ZERO;
		BigDecimal disconnectionCreditForDays = BigDecimal.ZERO;
		BigDecimal netAmount = BigDecimal.ZERO;
		int numberOfDays = 0;
		int totalDays = 0;

		this.startDate = disconnectionDate;
		this.endDate = new LocalDate(billingOrderData.getInvoiceTillDate());
		invoiceTillDate = new LocalDate(billingOrderData.getInvoiceTillDate());
		int maxDaysOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
		int maximumDaysInYear = startDate.dayOfYear().withMaximumValue().getDayOfYear();
		LocalDate nextDurationDate = startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
		totalDays = Days.daysBetween(startDate, nextDurationDate).getDays() + 1;
		int numberOfMonths = Months.monthsBetween(disconnectionDate,invoiceTillDate).getMonths();

		if (billingOrderData.getBillingAlign().equalsIgnoreCase("N")) {
			LocalDate tempBillEndDate = invoiceTillDate.minusMonths(numberOfMonths);
			numberOfDays = Days.daysBetween(disconnectionDate, tempBillEndDate).getDays()+1;

		} else if (billingOrderData.getBillingAlign().equalsIgnoreCase("Y")) {
			if (numberOfMonths > 0) {
				LocalDate tempBillEndDate = invoiceTillDate.minusMonths(numberOfMonths).dayOfMonth().withMaximumValue();
				numberOfDays = Days.daysBetween(disconnectionDate,tempBillEndDate).getDays()+1;
			} else {
				/* LocalDate tempBillEndDate =invoiceTillDate.minusMonths(numberOfMonths).dayOfMonth().withMaximumValue()*/;
				numberOfDays = Days.daysBetween(disconnectionDate,invoiceTillDate).getDays()+1;
			}
		}

		// calculate amount for one month
		if (numberOfMonths != 0) {
			if (billingOrderData.getChargeDuration() == 12) {
				netAmount = price.divide(new BigDecimal(billingOrderData.getChargeDuration()),RoundingMode.HALF_UP);
				disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
			} else if (billingOrderData.getChargeDuration() != 1) {
				netAmount = price.divide(new BigDecimal(billingOrderData.getChargeDuration()),RoundingMode.HALF_UP);
				disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
			} else {
				disconnectionCreditForMonths = price.multiply(new BigDecimal(numberOfMonths));// monthly
			}
		}

		// calculate amount for one Day
		if (billingOrderData.getChargeDuration() == 12) {
			disconnectionCreditPerday = price.divide(new BigDecimal(maximumDaysInYear), RoundingMode.HALF_UP);
		} else if (billingOrderData.getChargeDuration() != 1) {
			disconnectionCreditPerday = price.divide(new BigDecimal(totalDays),RoundingMode.HALF_UP);
		} else {
			disconnectionCreditPerday = price.divide(new BigDecimal(maxDaysOfMonth), RoundingMode.HALF_UP);
		}

		if (numberOfDays != 0) {
			disconnectionCreditForDays = disconnectionCreditPerday.multiply(new BigDecimal(numberOfDays));
		}
		return price = disconnectionCreditForMonths.add(disconnectionCreditForDays);// final case

	}

	// Calcualate Weekly DisconnectionCredit 
	public BigDecimal calcualateWeeklyDisconnectionCredit(BillingOrderData billingOrderData, LocalDate disconnectionDate) {
		
		BigDecimal disconnectionCreditForWeeks = BigDecimal.ZERO;
		BigDecimal disconnectionCreditPerday = BigDecimal.ZERO;
		BigDecimal disconnectionCreditForDays = BigDecimal.ZERO;
		BigDecimal netAmount = BigDecimal.ZERO;
		int numberOfDays = 0;
		
		this.startDate = disconnectionDate;
		this.endDate = new LocalDate(billingOrderData.getInvoiceTillDate());
		invoiceTillDate = new LocalDate(billingOrderData.getInvoiceTillDate());
		int numberOfWeeks = Weeks.weeksBetween(disconnectionDate,invoiceTillDate).getWeeks();

		if (billingOrderData.getBillingAlign().equalsIgnoreCase("N")) {
			LocalDate tempBillEndDate = invoiceTillDate.minusWeeks(numberOfWeeks);
			numberOfDays = Days.daysBetween(disconnectionDate,tempBillEndDate).getDays();

		} else if (billingOrderData.getBillingAlign().equalsIgnoreCase("Y")) {
			LocalDate tempBillEndDate = invoiceTillDate.minusWeeks(numberOfWeeks).dayOfWeek().withMaximumValue();
			numberOfDays = Days.daysBetween(disconnectionDate,tempBillEndDate).getDays();
		}

		if (numberOfWeeks != 0) {
			if (billingOrderData.getChargeDuration() == 2) {
				netAmount = price.divide(new BigDecimal(billingOrderData.getChargeDuration()), RoundingMode.HALF_UP);
				disconnectionCreditForWeeks = netAmount.multiply(new BigDecimal(numberOfWeeks));
			} else {
				disconnectionCreditForWeeks = price.multiply(new BigDecimal(numberOfWeeks));
			}
		}
		disconnectionCreditPerday = this.getWeeklyPricePerDay(billingOrderData.getChargeDuration(), price);
		if (numberOfDays != 0) {
			disconnectionCreditForDays = disconnectionCreditPerday.multiply(new BigDecimal(numberOfDays));
		}
	
		
	  return price = disconnectionCreditForWeeks.add(disconnectionCreditForDays);
		
	}

	private boolean IsDiscountOrPromotionApplicable(final BillingOrderData billingOrderData,final DiscountMasterData discountMasterData) {

		boolean IsDiscountOrPromotionApplicable = false;

		if (billingOrderData.getInvoiceTillDate().before(this.generateBill.getDiscountEndDateIfNull(discountMasterData, new LocalDate(billingOrderData.getInvoiceTillDate()))) 
				|| billingOrderData.getInvoiceTillDate().equals(this.generateBill.getDiscountEndDateIfNull(discountMasterData,new LocalDate(billingOrderData.getInvoiceTillDate())))) {
			IsDiscountOrPromotionApplicable = true;
		}

		return IsDiscountOrPromotionApplicable;
	}
	
	private boolean IsDiscountOrPromotionEndedBeforeOrderInvoice(final BillingOrderData billingOrderData,final DiscountMasterData discountMasterData) {
		
		boolean isDiscountEndedBeforeOrderInvoice = false;
		 
		 if (discountMasterData.getDiscountEndDate() != null && billingOrderData.getInvoiceTillDate().after(discountMasterData.getDiscountEndDate().toDate())){
			     isDiscountEndedBeforeOrderInvoice = true;
			}
		
		return isDiscountEndedBeforeOrderInvoice;
	}

	
}