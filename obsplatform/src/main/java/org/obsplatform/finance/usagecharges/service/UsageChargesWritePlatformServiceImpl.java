
package org.obsplatform.finance.usagecharges.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.billingorder.api.BillingTransactionConstants;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.domain.BillingOrder;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.service.GenerateBill;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.finance.usagecharges.domain.UsageCharge;
import org.obsplatform.finance.usagecharges.domain.UsageChargeRepository;
import org.obsplatform.finance.usagecharges.domain.UsageRaWDataRepository;
import org.obsplatform.finance.usagecharges.domain.UsageRaw;
import org.obsplatform.finance.usagecharges.serialization.UsageChargesCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Ranjith
 * 
 */
@Service
public class UsageChargesWritePlatformServiceImpl implements UsageChargesWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(UsageChargesWritePlatformServiceImpl.class);
	
	private final UsageChargesCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final UsageRaWDataRepository usageRawDataRepository;
	private final UsageChargeRepository usageChargeRepository;
	private final UsageChargesReadPlatformService usageChargesReadPlatformService;
	private final GenerateBill generateBill;
	

	@Autowired
	public UsageChargesWritePlatformServiceImpl(final UsageChargesCommandFromApiJsonDeserializer apiJsonDeserializer,
			final UsageRaWDataRepository usageRawDataRepository,
			final UsageChargeRepository usageChargeRepository,
			final UsageChargesReadPlatformService usageChargesReadPlatformService,
			final GenerateBill generateBill) {
		
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.usageRawDataRepository = usageRawDataRepository;
		this.usageChargeRepository = usageChargeRepository;
		this.usageChargesReadPlatformService = usageChargesReadPlatformService;
		this.generateBill = generateBill;
		

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see createUsageChargesRawData(JsonCommand)
	 */
	@Override
	public CommandProcessingResult createUsageChargesRawData(final JsonCommand command) {

		try {
			this.apiJsonDeserializer.validateForCreate(command.json());
			final UsageRaw rawData = UsageRaw.fromJson(command);
			this.usageRawDataRepository.save(rawData);
			return new CommandProcessingResultBuilder().withEntityId(rawData.getId()).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see processCustomerUsageRawData(Long)
	 * Here Transactional annotation help to handling of 
	 * JPA/Hibernate: detached entity passed to persist execption(persistenceexception)
	 */
	@Transactional
	@Override
	public void processCustomerUsageRawData(final UsageChargesData customerData) {

		try {
			BigDecimal totalCost = BigDecimal.ZERO;
			BigDecimal totalDuration = BigDecimal.ZERO;

			 List<UsageRaw> rawDatas = this.usageRawDataRepository.findUsageRawDataByCustomerDetails(customerData.getClientId(),customerData.getNumber());

			if (rawDatas.size() != 0) {
				UsageCharge chargeData = new UsageCharge(customerData.getClientId(),customerData.getNumber(),DateUtils.getDateTimeOfTenant(), totalCost,totalDuration);
				
				for (UsageRaw rawData : rawDatas) {
					totalDuration = totalDuration.add(rawData.getDuration());
					totalCost = totalCost.add(rawData.getCost());
					chargeData.addUsageRaw(rawData);
				}
				chargeData.setTotalDuration(totalDuration);
				chargeData.setTotalCost(totalCost);
				this.usageChargeRepository.save(chargeData);
			}
		} catch (DataIntegrityViolationException dve) {
			logger.error("usage rawData process failed........\r\n" +dve.getMessage());
		}

	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final Exception dve) {

		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ dve.getMessage());

	}

	/** (non-Javadoc)
	 * @see #checkOrderUsageCharges(BillingOrderData)
	 */
	@Override
	public BillingOrderCommand checkOrderUsageCharges(final BillingOrderData billingOrderData) {
		
	 BigDecimal chargeAmount=BigDecimal.ZERO; 
	 LocalDate chargeStartDate = null;
	 LocalDate chargeEndDate = null;
	 LocalDate nextBillableDate=null;
	 List<InvoiceTaxCommand> listOfTaxes = new ArrayList<InvoiceTaxCommand>();
	 List<UsageChargesData> usageChargeDatas = new ArrayList<UsageChargesData>(); 
	 usageChargeDatas =  this.usageChargesReadPlatformService.retrieveOrderCdrData(billingOrderData.getClientId(), billingOrderData.getClientOrderId());

		if (!usageChargeDatas.isEmpty()) {

			for (UsageChargesData usageChargeData : usageChargeDatas) {
				chargeAmount = chargeAmount.add(usageChargeData.getTotalCost());
			}
			chargeAmount = chargeAmount.add(billingOrderData.getPrice()).setScale(Integer.parseInt(this.generateBill.roundingDecimal()),RoundingMode.HALF_UP);;
		}

		if (billingOrderData.getInvoiceTillDate() == null) {

			chargeStartDate = new LocalDate(billingOrderData.getBillStartDate());
			if ("Y".equalsIgnoreCase(billingOrderData.getBillingAlign())) {
				chargeEndDate = chargeStartDate.dayOfMonth().withMaximumValue();
				nextBillableDate= chargeEndDate.plusMonths(billingOrderData.getChargeDuration()).dayOfMonth().withMaximumValue().plusDays(1);	
			} else {
				chargeEndDate = chargeStartDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
				nextBillableDate= chargeEndDate.plusMonths(billingOrderData.getChargeDuration()+1).dayOfMonth().getLocalDate().plusDays(1);	
			}
						
		} else if (billingOrderData.getInvoiceTillDate() != null) {

			chargeStartDate = new LocalDate(billingOrderData.getInvoiceTillDate()).plusDays(1);
			chargeEndDate = chargeStartDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
		    nextBillableDate= chargeEndDate.plusMonths(billingOrderData.getChargeDuration()).dayOfMonth().withMaximumValue().plusDays(1);
		}
	 
		return new BillingOrderCommand(billingOrderData.getClientOrderId(),billingOrderData.getOderPriceId(),
				billingOrderData.getClientId(), chargeStartDate.toDate(), nextBillableDate.toDate(),
				chargeEndDate.toDate(),billingOrderData.getBillingFrequency(),billingOrderData.getChargeCode(),
				billingOrderData.getChargeType(),billingOrderData.getChargeDuration(),billingOrderData.getDurationType(),
				chargeEndDate.toDate(),chargeAmount, billingOrderData.getBillingFrequency(),
				listOfTaxes, billingOrderData.getStartDate(),billingOrderData.getEndDate(), null,
				billingOrderData.getTaxInclusive(),BillingTransactionConstants.INVOICE,usageChargeDatas);

	}

	/** (non-Javadoc)
	 * @see updateUsageCharges(List,Invoice)
	 */
	@Override
	public void updateUsageCharges(List<BillingOrderCommand> commands,Invoice invoice) {

		List<BillingOrder> charges = invoice.getCharges();
		for (BillingOrderCommand billingOrderCommand : commands) {

			if ("UC".equalsIgnoreCase(billingOrderCommand.getChargeType()) && !billingOrderCommand.getUsageChargeDatas().isEmpty()) {

				for (BillingOrder charge : charges) {
					if (charge.getOrderlineId().equals(billingOrderCommand.getOrderPriceId())) {
						for (UsageChargesData usage : billingOrderCommand.getUsageChargeDatas()) {
							UsageCharge  usageCharge = this.usageChargeRepository.findOne(usage.getId());
							usageCharge.setChargeId(charge.getId());
							this.usageChargeRepository.save(usageCharge);
						}
					}

				}

			}

		}
		
	}
	
}
	


