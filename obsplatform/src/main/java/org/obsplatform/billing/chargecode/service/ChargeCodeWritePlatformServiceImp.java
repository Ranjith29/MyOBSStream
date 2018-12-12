package org.obsplatform.billing.chargecode.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.chargecode.exception.ChargeCodeNotFoundException;
import org.obsplatform.billing.chargecode.serialization.ChargeCodeCommandFromApiJsonDeserializer;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.discountmaster.domain.DiscountMaster;
import org.obsplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.obsplatform.billing.planprice.domain.Price;
import org.obsplatform.billing.planprice.domain.PriceRepository;
import org.obsplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.service.GenerateBill;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 * 
 */
@Service
public class ChargeCodeWritePlatformServiceImp implements ChargeCodeWritePlatformService {

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(ChargeCodeWritePlatformServiceImp.class);

	private final PlatformSecurityContext context;
	private final ChargeCodeRepository chargeCodeRepository;
	private final ChargeCodeCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final DiscountMasterRepository discountMasterRepository;
	private final PriceRepository priceRepository;
    private final GenerateBill generateBill;
    private final ClientRepositoryWrapper clientRepository;
    
	@Autowired
	public ChargeCodeWritePlatformServiceImp(final PlatformSecurityContext context, final ChargeCodeRepository chargeCodeRepository,
			final ChargeCodeCommandFromApiJsonDeserializer apiJsonDeserializer, final DiscountMasterRepository discountMasterRepository,
			final PriceRepository priceRepository, final GenerateBill generateBill, final ClientRepositoryWrapper clientRepository) {
		
		this.context = context;
		this.chargeCodeRepository = chargeCodeRepository;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.discountMasterRepository = discountMasterRepository;
		this.priceRepository = priceRepository;
		this.generateBill = generateBill;
		this.clientRepository = clientRepository;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createChargeCode(org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Override
	public CommandProcessingResult createChargeCode(final JsonCommand command) {

		ChargeCodeMaster chargeCode = null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validaForCreate(command.json());
			chargeCode = ChargeCodeMaster.fromJson(command);
			this.chargeCodeRepository.save(chargeCode);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					  .withEntityId(chargeCode.getId()).build();
		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}

	private void handleDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("chargecode")) {
			throw new PlatformDataIntegrityException("error.msg.chargecode.duplicate.name", "A code with name'"
							+ command.stringValueOfParameterNamed("chargeCode")
							+ "'already exists", "chargeCode",
					command.stringValueOfParameterNamed("chargeCode"));
		}

		if (realCause.getMessage().contains("chargedescription")) {
			throw new PlatformDataIntegrityException("error.msg.chargecode.duplicate.name",
					"A description with name'"+ command.stringValueOfParameterNamed("charge_description")
							+ "'already exists", "chargeDescription",
					command.stringValueOfParameterNamed("charge_description"));
		}
		
		if (realCause.getMessage().contains("foreign key constraint")) {
			throw new PlatformDataIntegrityException("error.msg.chargecode.can not.delete or update already used",
					"A code with name'"	+ command.stringValueOfParameterNamed("chargeCode")
							+ "'already used", "chargeCode",
					command.stringValueOfParameterNamed("chargeCode"));
		}
		LOGGER.error(dve.getMessage(), dve);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #updateChargeCode(org.obsplatform.infrastructure.core.api.JsonCommand,
	 * java.lang.Long)
	 */
	@Override
	public CommandProcessingResult updateChargeCode(final JsonCommand command,final Long chargeCodeId) {
		
		ChargeCodeMaster chargeCode = null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validaForCreate(command.json());
			chargeCode = retrieveChargeCodeById(chargeCodeId);
			final Map<String, Object> changes = chargeCode.update(command);
			if (!changes.isEmpty()) {
				chargeCodeRepository.saveAndFlush(chargeCode);
			}

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(chargeCode.getId()).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			if (dve.getCause() instanceof ConstraintViolationException) {
				handleDataIntegrityIssues(command, dve);
			}
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}

	private ChargeCodeMaster retrieveChargeCodeById(final Long chargeCodeId) {
		
		final ChargeCodeMaster chargeCode = this.chargeCodeRepository.findOne(chargeCodeId);
		if (chargeCode == null) {
			throw new ChargeCodeNotFoundException(chargeCodeId.toString());
		}
		return chargeCode;
	}
	@Override
	public BigDecimal calculateFinalAmount(ChargeCodeData chargeCodeData, Long clientId, Long priceId) {
		
		Long defaultValue = Long.valueOf(0);
		Date defaultDate = DateUtils.getDateOfTenant();
		Price price = this.priceRepository.findOne(priceId);
		BigDecimal finalAmount = BigDecimal.ZERO;
		DiscountMaster discountMaster = this.discountMasterRepository.findOne(price.getDiscountId());
		LocalDate endDate = new LocalDate(discountMaster.getStartDate()).plusMonths(1);
		ChargeCodeMaster chargeCode = this.chargeCodeRepository.findOne(chargeCodeData.getId());
		Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
		BillingOrderData billingOrderData = new BillingOrderData(defaultValue, defaultValue, defaultValue, clientId, discountMaster.getStartDate(),
				defaultDate, defaultDate, chargeCodeData.getBillFrequencyCode(), chargeCode.getChargeCode(), chargeCode.getChargeType(), chargeCode.getChargeDuration(),
				chargeCode.getChargeType(), defaultDate, price.getPrice(), "", discountMaster.getStartDate(), defaultDate, defaultValue, chargeCode.getTaxInclusive(), String.valueOf(client.getTaxExemption())); 
		
		DiscountMasterData discountMasterData = new DiscountMasterData(discountMaster.getId(), defaultValue,defaultValue, new LocalDate(discountMaster.getStartDate()),
							DateUtils.getLocalDateOfTenant(), discountMaster.getDiscountType(), discountMaster.getDiscountRate(), "N", discountMaster.getDiscountCode(), discountMaster.getDiscountDescription());
		
		List<InvoiceTaxCommand> invoiceTaxCommands = this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData, new LocalDate(discountMaster.getStartDate()), endDate, price.getPrice());
		if(!invoiceTaxCommands.isEmpty()){
		finalAmount = invoiceTaxCommands.get(0).getDiscountedAmount();
		}else {
			finalAmount= price.getPrice();
		}
		if(chargeCode.getTaxInclusive() != 1){
		for(InvoiceTaxCommand invoiceTaxCommand:invoiceTaxCommands){
			finalAmount = finalAmount.add(invoiceTaxCommand.getTaxAmount()); 
		}
		}
		return finalAmount;
	}

}
