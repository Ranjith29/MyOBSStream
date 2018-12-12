package org.obsplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.data.GenerateInvoiceData;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.domain.InvoiceRepository;
import org.obsplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.obsplatform.finance.billingorder.serialization.BillingOrderCommandFromApiJsonDeserializer;
import org.obsplatform.finance.usagecharges.service.UsageChargesWritePlatformService;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class InvoiceClient {

	private final static Logger logger = LoggerFactory.getLogger(InvoiceClient.class);
	
	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final BillingOrderCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ConfigurationRepository globalConfigurationRepository;
	private final InvoiceRepository invoiceRepository;
	private final UsageChargesWritePlatformService usageChargesWritePlatformService;
	

	@Autowired
	InvoiceClient(final BillingOrderReadPlatformService billingOrderReadPlatformService,final GenerateBillingOrderService generateBillingOrderService,
			final BillingOrderWritePlatformService billingOrderWritePlatformService,final BillingOrderCommandFromApiJsonDeserializer apiJsonDeserializer,
		    final ConfigurationRepository globalConfigurationRepository,final InvoiceRepository invoiceRepository,
		    final UsageChargesWritePlatformService usageChargesWritePlatformService) {

		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateBillingOrderService = generateBillingOrderService;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.globalConfigurationRepository = globalConfigurationRepository;
		this.invoiceRepository = invoiceRepository;
		this.usageChargesWritePlatformService = usageChargesWritePlatformService;
	
	}
	
	public CommandProcessingResult createInvoiceBill(final JsonCommand command) {
		
		try {
			// validation check
			this.apiJsonDeserializer.validateForCreate(command.json());
			final LocalDate processDate = command.localDateValueOfParameterNamed("systemDate");
			Invoice invoice = this.invoicingSingleClient(command.entityId(),processDate);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(invoice.getId()).build();
		} catch (DataIntegrityViolationException dve) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}
	
	public Invoice invoicingSingleClient(final Long clientId, LocalDate processDate) {

		
		BigDecimal invoiceAmount = BigDecimal.ZERO;
		LocalDate initialProcessDate = processDate;
		Date nextBillableDate = null;
		GenerateInvoiceData invoiceData = null;
		Invoice invoice=null;

		// Get list of qualified orders of customer
		List<BillingOrderData> billingOrderDatas = billingOrderReadPlatformService.retrieveOrderIds(clientId, processDate);
		
		if (billingOrderDatas.size() != 0) {
			
			boolean prorataWithNextBillFlag = this.checkInvoiceConfigurations(ConfigurationConstants.CONFIG_PRORATA_WITH_NEXT_BILLING_CYCLE);
			boolean    singleInvoiceFlag    = this.checkInvoiceConfigurations(ConfigurationConstants.CONFIG_SINGLE_INVOICE_FOR_MULTI_ORDERS);
			
			for (BillingOrderData billingOrderData : billingOrderDatas) {
				
				nextBillableDate = billingOrderData.getNextBillableDate();
				if (prorataWithNextBillFlag && ("Y".equalsIgnoreCase(billingOrderData.getBillingAlign())) && billingOrderData.getInvoiceTillDate() == null ) {
					LocalDate alignEndDate = new LocalDate(nextBillableDate).dayOfMonth().withMaximumValue();
					if (!processDate.toDate().after(alignEndDate.toDate())) 
						processDate = alignEndDate.plusDays(2);
				} else {
					processDate = initialProcessDate;
				}
				while (processDate.toDate().after(nextBillableDate) || processDate.toDate().compareTo(nextBillableDate) == 0) {

					invoiceData = invoiceServices(billingOrderData,clientId,processDate,invoice,singleInvoiceFlag);
					
					if (invoiceData != null) {
						invoiceAmount = invoiceAmount.add(invoiceData.getInvoiceAmount());
						nextBillableDate = invoiceData.getNextBillableDay();
						invoice=invoiceData.getInvoice();
					}
				}
			}
			if (singleInvoiceFlag && invoiceData != null) {

				this.invoiceRepository.save(invoiceData.getInvoice());

				// Update Client Balance
				this.billingOrderWritePlatformService.updateClientBalance(invoiceData.getInvoice().getInvoiceAmount(), clientId,false);
			}
			if (invoiceData != null) {
				return invoiceData.getInvoice();
			} else {
				throw new BillingOrderNoRecordsFoundException();
			}
			
		} else {
			throw new BillingOrderNoRecordsFoundException();
		}
	}

	public GenerateInvoiceData invoiceServices(final BillingOrderData billingOrderData, final Long clientId,final LocalDate processDate,Invoice invoice,boolean singleInvoiceFlag) {

		// Get qualified order complete details
		List<BillingOrderData> products = this.billingOrderReadPlatformService.retrieveBillingOrderData(clientId, processDate,billingOrderData.getOrderId());

		List<BillingOrderCommand> billingOrderCommands = this.generateBillingOrderService.generateBillingOrder(products);

		if (billingOrderCommands.size() != 0 && singleInvoiceFlag) {

				invoice = this.generateBillingOrderService.generateMultiOrderInvoice(billingOrderCommands,invoice);

				// Update order-price
				this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
			
				logger.info("---------------------" + billingOrderCommands.get(0).getNextBillableDate());

				return new GenerateInvoiceData(clientId, billingOrderCommands.get(0).getNextBillableDate(),invoice.getInvoiceAmount(), invoice);

		} else if(billingOrderCommands.size() != 0){

				// Invoice
				Invoice singleInvoice = this.generateBillingOrderService.generateInvoice(billingOrderCommands);

				// Update order-price
				this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
				
				logger.info("---------------------" + billingOrderCommands.get(0).getNextBillableDate());

				// Update usage charge's with chargeId
				this.usageChargesWritePlatformService.updateUsageCharges(billingOrderCommands, singleInvoice);

				// Update Client Balance
				this.billingOrderWritePlatformService.updateClientBalance(singleInvoice.getInvoiceAmount(), clientId, false);

				return new GenerateInvoiceData(clientId, billingOrderCommands.get(0).getNextBillableDate(),singleInvoice.getInvoiceAmount(), singleInvoice);
				
		} else {
			return null;
		}
	}

	public Invoice singleOrderInvoice(final Long orderId, final Long clientId,final LocalDate processDate) {

		// Get qualified order complete details
		List<BillingOrderData> products = this.billingOrderReadPlatformService.retrieveBillingOrderData(clientId, processDate,orderId);

		List<BillingOrderCommand> billingOrderCommands = this.generateBillingOrderService.generateBillingOrder(products);
			
		// Invoice
		Invoice  invoice = this.generateBillingOrderService.generateInvoice(billingOrderCommands);

		// Update order-price
		this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);

		logger.info("---------------------" + billingOrderCommands.get(0).getNextBillableDate());

		// Update Client Balance
		this.billingOrderWritePlatformService.updateClientBalance(invoice.getInvoiceAmount(), clientId, false);
		
		return invoice;
	
	}
	
	public boolean checkInvoiceConfigurations(final String configName) {

		Configuration configuration = this.globalConfigurationRepository.findOneByName(configName);
		if (configuration != null && configuration.isEnabled()) {
			return true;
		} else {
			return false;
		}

	}

}
