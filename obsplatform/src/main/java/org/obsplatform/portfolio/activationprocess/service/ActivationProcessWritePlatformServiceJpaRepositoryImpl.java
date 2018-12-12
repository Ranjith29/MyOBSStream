/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.activationprocess.service;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.planprice.domain.Price;
import org.obsplatform.billing.planprice.domain.PriceRepository;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfCareRepository;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporary;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.obsplatform.billing.selfcare.exception.SelfCareNotVerifiedException;
import org.obsplatform.billing.selfcare.exception.SelfCareTemporaryEmailIdNotFoundException;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.codes.domain.CodeValue;
import org.obsplatform.infrastructure.codes.domain.CodeValueRepository;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.domain.ItemMaster;
import org.obsplatform.logistics.item.domain.ItemRepository;
import org.obsplatform.logistics.item.exception.ItemNotFoundException;
import org.obsplatform.logistics.itemdetails.domain.ItemDetails;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.obsplatform.logistics.itemdetails.exception.SerialNumberAlreadyExistException;
import org.obsplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleWritePlatformService;
import org.obsplatform.logistics.ownedhardware.service.OwnedHardwareWritePlatformService;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.address.exception.CityNotFoundException;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.service.MessagePlatformEmailService;
import org.obsplatform.portfolio.activationprocess.exception.ClientAlreadyCreatedException;
import org.obsplatform.portfolio.activationprocess.serialization.ActivationProcessCommandFromApiJsonDeserializer;
import org.obsplatform.portfolio.client.service.ClientIdentifierWritePlatformService;
import org.obsplatform.portfolio.client.service.ClientWritePlatformService;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
@Service
public class ActivationProcessWritePlatformServiceJpaRepositoryImpl implements ActivationProcessWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ActivationProcessWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final ItemRepository itemRepository;
    private final ClientWritePlatformService clientWritePlatformService;
    private final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService;
    private final OrderWritePlatformService orderWritePlatformService;
    private final ConfigurationRepository configurationRepository;
	private final OwnedHardwareWritePlatformService ownedHardwareWritePlatformService;
	private final AddressReadPlatformService addressReadPlatformService;
	private final ActivationProcessCommandFromApiJsonDeserializer commandFromApiJsonDeserializer;
	private final ItemDetailsRepository itemDetailsRepository;
	private final SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	private final CodeValueRepository codeValueRepository;
	private final ClientIdentifierWritePlatformService clientIdentifierWritePlatformService;
	private final PriceRepository priceRepository;
	private final ChargeCodeRepository chargeCodeRepository;
	private final SelfCareRepository selfCareRepository;
	private final ContractRepository contractRepository;
	private final PlanRepository planRepository;
	
	
    @Autowired
    public ActivationProcessWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final FromJsonHelper fromJsonHelper,
    		final ClientWritePlatformService clientWritePlatformService,final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService,
    		final OrderWritePlatformService orderWritePlatformService,final ConfigurationRepository globalConfigurationRepository,
    		final OwnedHardwareWritePlatformService ownedHardwareWritePlatformService, final AddressReadPlatformService addressReadPlatformService,
    		final ActivationProcessCommandFromApiJsonDeserializer commandFromApiJsonDeserializer, final ItemDetailsRepository itemDetailsRepository,
    		final SelfCareTemporaryRepository selfCareTemporaryRepository,final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService,
    		final CodeValueRepository codeValueRepository,final ItemRepository itemRepository,final PriceRepository priceRepository,
    		final BillingMessageTemplateRepository billingMessageTemplateRepository,final MessagePlatformEmailService messagePlatformEmailService,
    		final BillingMessageRepository messageDataRepository,final ClientIdentifierWritePlatformService clientIdentifierWritePlatformService,
    		final ChargeCodeRepository chargeCodeRepository,final SelfCareRepository selfCareRepository,final ContractRepository contractRepository,
    		final PlanRepository planRepository) {

        
    	this.context = context;
    	this.itemRepository=itemRepository;
        this.fromJsonHelper = fromJsonHelper;
        this.clientWritePlatformService = clientWritePlatformService;
        this.oneTimeSaleWritePlatformService = oneTimeSaleWritePlatformService;
        this.orderWritePlatformService = orderWritePlatformService;
        this.configurationRepository = globalConfigurationRepository;
        this.ownedHardwareWritePlatformService = ownedHardwareWritePlatformService;
        this.addressReadPlatformService = addressReadPlatformService;
        this.commandFromApiJsonDeserializer = commandFromApiJsonDeserializer;
        this.itemDetailsRepository = itemDetailsRepository;
        this.priceRepository = priceRepository;
        this.chargeCodeRepository = chargeCodeRepository;
        this.selfCareTemporaryRepository = selfCareTemporaryRepository;
        this.contractRepository = contractRepository;
        this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
        this.selfCareRepository = selfCareRepository;
        this.codeValueRepository = codeValueRepository;
        this.clientIdentifierWritePlatformService = clientIdentifierWritePlatformService;
        this.planRepository = planRepository;
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }else if (realCause.getMessage().contains("email_key")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.email", "Client with email `" + email
                    + "` already exists", "email", email);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult activationProcess(final JsonCommand command) {

        try {
            context.authenticatedUser();
            CommandProcessingResult resultClient=null;
            CommandProcessingResult resultSale=null;
            CommandProcessingResult resultOrder=null;
            final JsonElement element = fromJsonHelper.parse(command.json());
	        JsonArray clientData = fromJsonHelper.extractJsonArrayNamed("client", element);
	        JsonArray saleData = fromJsonHelper.extractJsonArrayNamed("sale", element);
	        JsonArray owndevices= fromJsonHelper.extractJsonArrayNamed("owndevice", element);
	        JsonArray bookOrder = fromJsonHelper.extractJsonArrayNamed("bookorder", element);
	        
	        
	       
	        for(JsonElement j:clientData){
           
	        	JsonCommand comm=new JsonCommand(null, j.toString(),j, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
	        	resultClient=this.clientWritePlatformService.createClient(comm);
	        }

	        if(saleData.size() != 0){
	        	for(JsonElement sale:saleData){
	        	  JsonCommand comm=new JsonCommand(null, sale.toString(),sale, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
	        	  resultSale=this.oneTimeSaleWritePlatformService.createOneTimeSale(comm,resultClient.getClientId());
	           }
	        }
	        else if(owndevices.size() != 0){
	        	for(JsonElement ownDevice:owndevices){
	        		
	        		  JsonCommand comm=new JsonCommand(null, ownDevice.toString(),ownDevice, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
		        	  resultSale=this.ownedHardwareWritePlatformService.createOwnedHardware(comm,resultClient.getClientId());
	        	}
	        	
	        }
	       
	         for(JsonElement order:bookOrder){
		        	
		        	JsonCommand comm=new JsonCommand(null, order.toString(),order, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
		        	resultOrder=this.orderWritePlatformService.createOrder(resultClient.getClientId(),comm);
		           
	         }
	        return resultClient;

           
        } catch (DataIntegrityViolationException dve) {
        	
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
	
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }


    @Transactional
	@Override
	public CommandProcessingResult selfRegistrationProcess(JsonCommand command) {

		try {
			
			context.authenticatedUser();
			Configuration deviceStatusConfiguration = configurationRepository.
					findOneByName(ConfigurationConstants.CONFIR_PROPERTY_REGISTRATION_DEVICE);
			
			commandFromApiJsonDeserializer.validateForCreate(command.json(),deviceStatusConfiguration.isEnabled());
			Long id = new Long(1);		
			CommandProcessingResult resultClient = null;
			CommandProcessingResult resultSale = null;
			CommandProcessingResult resultOrder = null;
			CommandProcessingResult resultRedemption = null;
			String device = null;
			String dateFormat = "dd MMMM yyyy";
			String activationDate = new SimpleDateFormat(dateFormat).format(DateUtils.getDateOfTenant());

			String fullname = command.stringValueOfParameterNamed("fullname");
			String firstName = command.stringValueOfParameterNamed("firstname");
			String lastname = command.stringValueOfParameterNamed("lastname");
			String city = command.stringValueOfParameterNamed("city");
			String address = command.stringValueOfParameterNamed("address");
			Long phone = command.longValueOfParameterNamed("phone");	
			Long homePhoneNumber = command.longValueOfParameterNamed("homePhoneNumber");	
			String email = command.stringValueOfParameterNamed("email");
			String nationalId = command.stringValueOfParameterNamed("nationalId");
			String deviceId = command.stringValueOfParameterNamed("device");
			String deviceAgreementType = command.stringValueOfParameterNamed("deviceAgreementType");
			String password = command.stringValueOfParameterNamed("password");
			String isMailCheck=command.stringValueOfParameterNamed("isMailCheck");
			String passport=command.stringValueOfParameterNamed("passport");
			Long planId = command.longValueOfParameterNamed("planId");
			String duration =command.stringValueOfParameterNamed("duration");
			SelfCareTemporary temporary =null;
			
			if(isMailCheck == null || isMailCheck.isEmpty()){
				 temporary = selfCareTemporaryRepository.findOneByEmailId(email);
				
				if(temporary == null){
					throw new SelfCareTemporaryEmailIdNotFoundException(email);
				
				}else if (temporary.getStatus().equalsIgnoreCase("ACTIVE")) {
					throw new ClientAlreadyCreatedException();
				
				}else if (temporary.getStatus().equalsIgnoreCase("INACTIVE")) {
  				throw new SelfCareNotVerifiedException(email);			
				}
			}
			
		//	if (temporary.getStatus().equalsIgnoreCase("PENDING")){
				
				String zipCode = command.stringValueOfParameterNamed("zipCode");
				// client creation
				AddressData addressData = this.addressReadPlatformService.retrieveAdressBy(city);
				if(addressData == null){
					throw new CityNotFoundException(city);
				}
				CodeValue codeValue=this.codeValueRepository.findOneByCodeValue("Normal");
				JSONObject clientcreation = new JSONObject();
				clientcreation.put("officeId", new Long(1));
				clientcreation.put("clientCategory", codeValue.getId());
				clientcreation.put("firstname",firstName);
				if(fullname == null || fullname.isEmpty()){
					clientcreation.put("lastname", lastname);
				}else{
				clientcreation.put("lastname", fullname);
				}
				clientcreation.put("phone", phone);
				clientcreation.put("homePhoneNumber", homePhoneNumber);
				clientcreation.put("entryType","IND");// new Long(1));
				clientcreation.put("addressNo", address);
				clientcreation.put("city", addressData.getCity());
				clientcreation.put("state", addressData.getState());
				clientcreation.put("country", addressData.getCountry());
				clientcreation.put("email", email);
				clientcreation.put("locale", "en");
				clientcreation.put("active", true);
				clientcreation.put("dateFormat", dateFormat);
				clientcreation.put("activationDate", activationDate);
				clientcreation.put("flag", false);
				clientcreation.put("zipCode", zipCode);
				clientcreation.put("device", deviceId);
				clientcreation.put("password", password);
				
				if(nationalId !=null && !nationalId.equalsIgnoreCase("")){
					clientcreation.put("externalId", nationalId);
				}

				final JsonElement element = fromJsonHelper.parse(clientcreation.toString());
				JsonCommand clientCommand = new JsonCommand(null,clientcreation.toString(), element, fromJsonHelper,
						null, null, null, null, null, null, null, null, null, null, 
						null, null);
				resultClient = this.clientWritePlatformService.createClient(clientCommand);

				if (resultClient == null) {
					throw new PlatformDataIntegrityException("error.msg.client.creation.failed", "Client Creation Failed","Client Creation Failed");
				}
				logger.info("responseOfClient: "+resultClient);
				if(passport != null && !passport.equalsIgnoreCase("")){
					CodeValue passportcodeValue=this.codeValueRepository.findOneByCodeValue("Passport");
					JSONObject clientIdentifierJson = new JSONObject();
					clientIdentifierJson.put("documentTypeId", passportcodeValue.getId());
					clientIdentifierJson.put("documentKey", passport);
					final JsonElement idenfierJsonEement = fromJsonHelper.parse(clientIdentifierJson.toString());
					JsonCommand idenfierJsonCommand = new JsonCommand(null,clientIdentifierJson.toString(), idenfierJsonEement, fromJsonHelper,
							null, null, null, null, null, null, null, null, null, null, 
							null, null);
					this.clientIdentifierWritePlatformService.addClientIdentifier(resultClient.getClientId(), idenfierJsonCommand);
				}
				
				if(temporary != null){
				temporary.setStatus("ACTIVE");
				this.selfCareTemporaryRepository.saveAndFlush(temporary);
				}
				
				//book device
				if (deviceStatusConfiguration != null) {

					if (deviceStatusConfiguration.isEnabled()) {

						JSONObject bookDevice = new JSONObject();
						/*deviceStatusConfiguration = configurationRepository
								.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_DEVICE_AGREMENT_TYPE);*/

						/*if (deviceStatusConfiguration != null&& deviceStatusConfiguration.isEnabled()
								&& deviceStatusConfiguration.getValue().equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_SALE)) {*/
						if(deviceAgreementType.equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_SALE)){
							
							device = command.stringValueOfParameterNamed("device");
							ItemDetails detail = itemDetailsRepository.getInventoryItemDetailBySerialNum(device);

							if (detail == null) {
								throw new SerialNumberNotFoundException(device);
							}

							ItemMaster itemMaster = this.itemRepository.findOne(detail.getItemMasterId());

							if (itemMaster == null) {
								throw new ItemNotFoundException(deviceId);
							}

							if (detail != null && detail.getStatus().equalsIgnoreCase("Used")) {
								throw new SerialNumberAlreadyExistException(device);
							}

							JSONObject serialNumberObject = new JSONObject();
							serialNumberObject.put("serialNumber", device);
							serialNumberObject.put("clientId", resultClient.getClientId());
							serialNumberObject.put("status", "allocated");
							serialNumberObject.put("itemMasterId", detail.getItemMasterId());
							serialNumberObject.put("isNewHw", "Y");
							JSONArray serialNumber = new JSONArray();
							serialNumber.put(0, serialNumberObject);

							bookDevice.put("chargeCode", itemMaster.getChargeCode());
							bookDevice.put("unitPrice", itemMaster.getUnitPrice());
							bookDevice.put("itemId", itemMaster.getId());
							bookDevice.put("discountId", id);
							bookDevice.put("officeId", detail.getOfficeId());
							bookDevice.put("totalPrice", itemMaster.getUnitPrice());

							bookDevice.put("quantity", id);
							bookDevice.put("locale", "en");
							bookDevice.put("dateFormat", dateFormat);
							bookDevice.put("saleType", "NEWSALE");
							bookDevice.put("saleDate", activationDate);
							bookDevice.put("serialNumber", serialNumber);

							final JsonElement deviceElement = fromJsonHelper.parse(bookDevice.toString());
							JsonCommand comm = new JsonCommand(null, bookDevice.toString(), deviceElement, fromJsonHelper,
									null, null, null, null, null, null, null, null, null, null, null, null);

							resultSale = this.oneTimeSaleWritePlatformService.createOneTimeSale(comm, resultClient.getClientId());

							if (resultSale == null) {
								throw new PlatformDataIntegrityException("error.msg.client.device.assign.failed", 
										"Device Assign Failed for ClientId :" + resultClient.getClientId(), "Device Assign Failed");
							}
							logger.info("responseOfSale: "+resultSale);
						} else if(deviceAgreementType.equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_OWN)){

							List<ItemMaster> itemMaster = this.itemRepository.findAll();
							bookDevice.put("locale", "en");
							bookDevice.put("dateFormat", dateFormat);
							bookDevice.put("allocationDate", activationDate);
							bookDevice.put("provisioningSerialNumber", deviceId);
							bookDevice.put("itemType", itemMaster.get(0).getId());
							bookDevice.put("serialNumber", deviceId);
							bookDevice.put("status", "ACTIVE");
							CommandWrapper commandWrapper = new CommandWrapperBuilder().createOwnedHardware(resultClient.getClientId()).withJson(bookDevice.toString()).build();
							final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(commandWrapper);

							if (result == null) {
								throw new PlatformDataIntegrityException("error.msg.client.device.assign.failed",
										"Device Assign Failed for ClientId :" + resultClient.getClientId(), "Device Assign Failed");
							}
							logger.info("responseOfOwnHW: "+result);
						}else {
							
						}
					}
				}
  				
				// book order
				Configuration selfregistrationconfiguration = configurationRepository.findOneByName(ConfigurationConstants.CONFIR_PROPERTY_SELF_REGISTRATION);
				
				if (selfregistrationconfiguration != null && selfregistrationconfiguration.isEnabled()) {
						
					if (selfregistrationconfiguration.getValue() != null && !selfregistrationconfiguration.getValue().isEmpty()) {

						JSONObject ordeJson = new JSONObject(selfregistrationconfiguration.getValue());
						ordeJson.put("locale", "en");
						ordeJson.put("isNewplan", true);
						ordeJson.put("dateFormat", dateFormat);
						ordeJson.put("start_date", activationDate);

						CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(resultClient.getClientId()).withJson(ordeJson.toString()).build();
						resultOrder = this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);

						if (resultOrder == null) {
							throw new PlatformDataIntegrityException("error.msg.client.order.creation", "Book Order Failed for ClientId:"	
									+ resultClient.getClientId(), "Book Order Failed");
						}
						logger.info("responseOfOrder: "+resultOrder);
					} else {

						String paytermCode = command.stringValueOfParameterNamed("paytermCode");
						String  contractPeriod = command.stringValueOfParameterNamed("contractPeriod");
						Long planCode = command.longValueOfParameterNamed("planCode");
						Contract contract =this.contractRepository.findOneByContractId(contractPeriod);
						List<Price> prices=this.priceRepository.findChargeCodeByPlanAndContract(planCode,contractPeriod);
						Plan planName = this.planRepository.findOne(planCode);
						if(planName == null || planName.isDeleted() == 'Y' ){
							
							throw new PlatformDataIntegrityException("error.msg.order.id.not.exist",
									"Plan doesn't exist with this id " + planCode, "plan code not exist");
						}
						Contract contractId = this.contractRepository.findOneByContractId(contractPeriod);
						if(contractId == null){
							throw new PlatformDataIntegrityException("error.msg.contractperiod.not.exist",
									"Contract Period doesn't exist with this contractPeriod " + contractPeriod, "Contract Period not exist");
						}
						
						if(!prices.isEmpty()){
							ChargeCodeMaster chargeCodeMaster = this.chargeCodeRepository.findOneByChargeCode(prices.get(0).getChargeCode());	
						if(chargeCodeMaster != null){
						 	paytermCode = chargeCodeMaster.getBillFrequencyCode();
						}
						}else if(prices.isEmpty()){
							throw new PlatformDataIntegrityException("error.msg.prices.not.exist",
									"Plan Price is not define with this Duration " + contractPeriod, "Plan Price is not define with this Duration");
						}
						if(contract != null){
						contractPeriod = contract.getId().toString();	
						}

						JSONObject ordeJson = new JSONObject();

						ordeJson.put("planCode", planCode);
						ordeJson.put("contractPeriod", contractPeriod);
						ordeJson.put("paytermCode", paytermCode);
						ordeJson.put("billAlign", false);
						ordeJson.put("locale", "en");
						ordeJson.put("isNewplan", true);
						ordeJson.put("dateFormat", dateFormat);
						ordeJson.put("start_date", activationDate);

						CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(resultClient.getClientId()).withJson(ordeJson.toString()).build();
						resultOrder = this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);

						if (resultOrder == null) {
							throw new PlatformDataIntegrityException("error.msg.client.order.creation",
									"Book Order Failed for ClientId:" + resultClient.getClientId(), "Book Order Failed");
						}
						logger.info("responseOfOrder: "+resultOrder);
					} 		
				}
				
				//redemption creation
				Configuration isRedemptionconfiguration = configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_REDEMPTION);
				
				if (isRedemptionconfiguration != null && isRedemptionconfiguration.isEnabled()) {

					
					JSONObject redemptionJson = new JSONObject();
					redemptionJson.put("clientId", resultClient.getClientId());
					redemptionJson.put("pinNumber",command.stringValueOfParameterNamed("pinNumber"));
					CommandWrapper commandRequest = new CommandWrapperBuilder().createRedemption().withJson(redemptionJson.toString()).build();
					resultRedemption = this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);
					if (resultRedemption == null) {
						throw new PlatformDataIntegrityException("error.msg.redemption.creation",
								"Redemption Failed for ClientId:" + resultClient.getClientId(), "Redemption Failed");
					}
					logger.info("responseOfRedemption: "+resultRedemption);
				}
				SelfCare selfCare  =this.selfCareRepository.findOneByClientId(resultClient.getClientId());
				final Map<String, Object> changes = new LinkedHashMap<String, Object>(1);
				changes.put("username", selfCare.getUserName());
				changes.put("password", selfCare.getPassword());
  				return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(resultClient.getClientId()) //
  		         .with(changes) //
  		         .build();
  			/*}  else {
  				return new CommandProcessingResult(Long.valueOf(-1));
  			}*/	


  		} catch (DataIntegrityViolationException dve) {
  			handleDataIntegrityIssues(command, dve);
  			return new CommandProcessingResult(Long.valueOf(-1));
  		} catch (JSONException e) {
  			return new CommandProcessingResult(Long.valueOf(-1));
  		}

  	}
}
