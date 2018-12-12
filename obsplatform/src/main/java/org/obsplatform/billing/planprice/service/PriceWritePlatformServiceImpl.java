package org.obsplatform.billing.planprice.service;

import java.util.List;
import java.util.Map;

import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.planprice.domain.Price;
import org.obsplatform.billing.planprice.domain.PriceRepository;
import org.obsplatform.billing.planprice.exceptions.ChargeCOdeExists;
import org.obsplatform.billing.planprice.exceptions.ChargeCodeAndContractPeriodException;
import org.obsplatform.billing.planprice.exceptions.ContractNotNullException;
import org.obsplatform.billing.planprice.exceptions.PriceNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.voucher.data.VoucherData;
import org.obsplatform.organisation.voucher.exception.AlreadyProcessedException;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.service.serialization.PriceCommandFromApiJsonDeserializer;
import org.obsplatform.portfolio.service.service.ServiceMasterWritePlatformServiceImpl;
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
public class PriceWritePlatformServiceImpl implements PriceWritePlatformService {

	 private final static Logger LOGGER = LoggerFactory.getLogger(ServiceMasterWritePlatformServiceImpl.class);
	 private final PlatformSecurityContext context;
	 private final PriceReadPlatformService priceReadPlatformService;
	 private final PriceCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	 private final PriceRepository priceRepository;
	 private final ChargeCodeRepository chargeCodeRepository;
	 private final ContractRepository contractRepository;
	 
	@Autowired
	 public PriceWritePlatformServiceImpl(final PlatformSecurityContext context,final PriceReadPlatformService priceReadPlatformService,
			 final PriceCommandFromApiJsonDeserializer fromApiJsonDeserializer,final PriceRepository priceRepository,
			 final ChargeCodeRepository chargeCodeRepository,final ContractRepository contractRepository)
		{
			this.context=context;
			this.priceReadPlatformService=priceReadPlatformService;
			this.fromApiJsonDeserializer=fromApiJsonDeserializer;
			this.priceRepository=priceRepository;
			this.chargeCodeRepository = chargeCodeRepository;
			this.contractRepository = contractRepository;
		}
	
	@Override
	public CommandProcessingResult createPricing(final Long planId,JsonCommand command) {
		
		try{
		context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		String isPrepaid = command.stringValueOfParameterNamed("isPrepaid");
		String chargeCode = command.stringValueOfParameterNamed("chargeCode");
		String contractPeriod = command.stringValueOfParameterNamed("duration");
		
		ChargeCodeMaster chargeCodeMaster = chargeCodeRepository.findOneByChargeCode(chargeCode);
		Contract contract = contractRepository.findOneByContractId(contractPeriod);
			if (isPrepaid.equalsIgnoreCase("Y") && chargeCodeMaster != null && contract != null) {
			
			if(chargeCodeMaster.getChargeDuration() != contract.getUnits().intValue() ||
					!chargeCodeMaster.getDurationType().equalsIgnoreCase(contract.getSubscriptionType())){
				throw new ChargeCodeAndContractPeriodException(chargeCode);
			}
		}
		List<ServiceData> serviceData = this.priceReadPlatformService.retrieveServiceCodeDetails(planId);
		
		final Price price =Price.fromJson(command,serviceData,planId);
			
			for (ServiceData data : serviceData) {
				
					if (data.getChargeCode() != null && data.getPlanId() == planId && data.getServiceCode().equalsIgnoreCase(price.getServiceCode())
							&& data.getPriceregion().equalsIgnoreCase(price.getPriceRegion().toString()) && data.getChargeCode().equalsIgnoreCase(price.getChargeCode())){
						
						throw new ChargeCOdeExists(data.getChargeDescription());
					}
			}
		this.priceRepository.save(price);
		return new CommandProcessingResult(price.getId());

	} catch (DataIntegrityViolationException dve) {
		LOGGER.error(dve.getMessage());
		 handleCodeDataIntegrityIssues(command, dve);
		return  CommandProcessingResult.empty();
	}
}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,DataIntegrityViolationException dve) {
		LOGGER.error(dve.getMessage(),dve);
		 throw new PlatformDataIntegrityException("error.msg.planprice.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource.");
		
	}
	@Override
	public CommandProcessingResult updatePrice(final Long priceId, JsonCommand command) {
		
		try{
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			String isPrepaid = command.stringValueOfParameterNamed("isPrepaid");
			String chargeCode = command.stringValueOfParameterNamed("chargeCode");
			String contractPeriod = command.stringValueOfParameterNamed("duration");
			
			ChargeCodeMaster chargeCodeMaster = chargeCodeRepository.findOneByChargeCode(chargeCode);
			Contract contract = contractRepository.findOneByContractId(contractPeriod);
			if(isPrepaid.equalsIgnoreCase("Y")){
				if(contract == null){
					throw new ContractNotNullException();
				}
				if(chargeCodeMaster.getChargeDuration() != contract.getUnits().intValue() ||
						!chargeCodeMaster.getDurationType().equalsIgnoreCase(contract.getSubscriptionType())){
					throw new ChargeCodeAndContractPeriodException(chargeCode);
				}
			}
				
			final Price price = retrievePriceBy(priceId);
			final  Map<String, Object> changes = price.update(command);
			if (changes.containsKey("duration")) {
				
				final List<VoucherData> voucherData= this.priceReadPlatformService.retrieveVoucherDatas(priceId);
				if(!voucherData.isEmpty()){
					throw new AlreadyProcessedException(price.getContractPeriod(),priceId);
				}
			}
			if(!changes.isEmpty())
				this.priceRepository.save(price);
  
			return new CommandProcessingResultBuilder() //
			.withCommandId(command.commandId()) //
			.withEntityId(priceId) //
			.with(changes) //
			.build();

         } catch (DataIntegrityViolationException dve) {
    		 handleCodeDataIntegrityIssues(command, dve);
    		return  CommandProcessingResult.empty();
    	}
	
	
}
	private Price retrievePriceBy(final Long priceId) {
		
		final Price price=this.priceRepository.findOne(priceId);
		if(price==null){
			{ throw new PriceNotFoundException(priceId.toString()); }
		}
		return price;
	}
	@Override
	public CommandProcessingResult deletePrice(final Long priceId) {
		  try {
				 Price price=this.priceRepository.findOne(priceId);
				 	if(price!= null){
				 		final List<VoucherData> voucherData= this.priceReadPlatformService.retrieveVoucherDatas(priceId);
						if(!voucherData.isEmpty()){
							throw new AlreadyProcessedException(price.getContractPeriod(),priceId);
						}
						price.delete();	
				 	}
			     this.priceRepository.save(price);
			     return new CommandProcessingResultBuilder().withEntityId(priceId).build();
		  	
		  } catch (DataIntegrityViolationException dve) {
			  handleCodeDataIntegrityIssues(null, dve);
		  		return new CommandProcessingResultBuilder().withEntityId(Long.valueOf(-1)).build();
			}
		  
		  }
}
