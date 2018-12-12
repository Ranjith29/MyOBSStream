/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.SQLGrammarException;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfCareRepository;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporary;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.obsplatform.billing.selfcare.service.SelfCareWritePlatformService;
import org.obsplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.billingorder.api.BillingTransactionConstants;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.infrastructure.codes.domain.CodeValue;
import org.obsplatform.infrastructure.codes.domain.CodeValueRepository;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.configuration.exception.ConfigurationPropertyNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.domain.Base64EncodedImage;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.domain.StatusTypeEnum;
import org.obsplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.obsplatform.logistics.onetimesale.service.InvoiceOneTimeSale;
import org.obsplatform.organisation.address.domain.Address;
import org.obsplatform.organisation.address.domain.AddressRepository;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.obsplatform.organisation.feemaster.service.FeeMasterReadplatformService;
import org.obsplatform.organisation.groupsdetails.domain.GroupsDetails;
import org.obsplatform.organisation.groupsdetails.domain.GroupsDetailsRepository;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeRepository;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.obsplatform.portfolio.client.api.ClientApiConstants;
import org.obsplatform.portfolio.client.data.ClientDataValidator;
import org.obsplatform.portfolio.client.domain.AccountNumberGenerator;
import org.obsplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientAdditionalFieldsRepository;
import org.obsplatform.portfolio.client.domain.ClientAdditionalfields;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.obsplatform.portfolio.client.domain.ClientStatus;
import org.obsplatform.portfolio.client.exception.ClientAdditionalDataNotFoundException;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.client.exception.InvalidClientStateTransitionException;
import org.obsplatform.portfolio.group.domain.Group;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.portfolio.property.domain.PropertyHistoryRepository;
import org.obsplatform.portfolio.property.domain.PropertyMaster;
import org.obsplatform.portfolio.property.domain.PropertyMasterRepository;
import org.obsplatform.portfolio.property.domain.PropertyTransactionHistory;
import org.obsplatform.portfolio.property.exceptions.PropertyCodeAllocatedException;
import org.obsplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.provisioning.domain.ServiceParameters;
import org.obsplatform.provisioning.provisioning.domain.ServiceParametersRepository;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.obsplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.obsplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
    private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
    private final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService;
    private final PlanRepository planRepository;
    private final OrderReadPlatformService orderReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ConfigurationRepository configurationRepository;
    private final ServiceParametersRepository serviceParametersRepository;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final ProvisioningWritePlatformService ProvisioningWritePlatformService;
    private final OrderRepository orderRepository;
    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final ClientAdditionalFieldsRepository clientAdditionalFieldsRepository;
    private final AddressRepository addressRepository;
    private final SelfCareRepository selfCareRepository;
    private final CodeValueRepository codeValueRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final GroupsDetailsRepository groupsDetailsRepository;
    private final ProvisioningActionsRepository provisioningActionsRepository;
    private final SelfCareWritePlatformService selfCareWritePlatformService;
    private final PropertyMasterRepository propertyMasterRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    private final SelfCareTemporaryRepository selfCareTemporaryRepository;
    private final FeeMasterReadplatformService feeMasterReadplatformService;
    private final InvoiceOneTimeSale invoiceOneTimeSale;
    private final JdbcTemplate jdbcTemplate;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientCardDetailsWritePlatformServiceJpaRepositoryImpl clientCardDetailsWritePlatformServiceJpaRepositoryImpl;
   

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final AddressRepository addressRepository,
            final ClientRepositoryWrapper clientRepository, final OfficeRepository officeRepository,final ClientDataValidator fromApiJsonDeserializer, 
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,final ServiceParametersRepository serviceParametersRepository,
            final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final ConfigurationRepository configurationRepository,
            final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final CodeValueRepository codeValueRepository,
            final OrderReadPlatformService orderReadPlatformService,final ProvisioningWritePlatformService  ProvisioningWritePlatformService,
            final GroupsDetailsRepository groupsDetailsRepository,final OrderRepository orderRepository,final PlanRepository planRepository,
            final PrepareRequestWriteplatformService prepareRequestWriteplatformService,final ClientReadPlatformService clientReadPlatformService,
            final SelfCareRepository selfCareRepository,final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService,
            final ProvisioningActionsRepository provisioningActionsRepository,final PrepareRequestReadplatformService prepareRequestReadplatformService,
            final SelfCareWritePlatformService selfCareWritePlatformService,final ClientAdditionalFieldsRepository clientAdditionalFieldsRepository,
            final PropertyMasterRepository propertyMasterRepository, final PropertyHistoryRepository propertyHistoryRepository,
            final SelfCareTemporaryRepository selfCareTemporaryRepository,final FeeMasterReadplatformService feeMasterReadplatformService,
            final InvoiceOneTimeSale invoiceOneTimeSale, final RoutingDataSource dataSource,
            final FromJsonHelper fromApiJsonHelper,
            final ClientCardDetailsWritePlatformServiceJpaRepositoryImpl clientCardDetailsWritePlatformServiceJpaRepositoryImpl) {
    	
        this.context = context;
        this.ProvisioningWritePlatformService=ProvisioningWritePlatformService;
        this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
        this.planRepository=planRepository;
        this.groupsDetailsRepository=groupsDetailsRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.orderReadPlatformService=orderReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.serviceParametersRepository = serviceParametersRepository;
        this.clientAdditionalFieldsRepository = clientAdditionalFieldsRepository;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.portfolioCommandSourceWritePlatformService=portfolioCommandSourceWritePlatformService;
        this.orderRepository=orderRepository;
        this.clientRepository = clientRepository;
        this.addressRepository=addressRepository;
        this.selfCareTemporaryRepository = selfCareTemporaryRepository;
        this.officeRepository = officeRepository;
        this.provisioningActionsRepository=provisioningActionsRepository;
        this.selfCareRepository=selfCareRepository;
        this.codeValueRepository=codeValueRepository;
        this.configurationRepository=configurationRepository;
        this.selfCareWritePlatformService = selfCareWritePlatformService;;
        this.propertyMasterRepository = propertyMasterRepository;
        this.propertyHistoryRepository = propertyHistoryRepository;
        this.feeMasterReadplatformService = feeMasterReadplatformService;
        this.invoiceOneTimeSale = invoiceOneTimeSale;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.clientCardDetailsWritePlatformServiceJpaRepositoryImpl =clientCardDetailsWritePlatformServiceJpaRepositoryImpl;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId,final JsonCommand command) {

        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);
            final CodeValue closureReason = this.codeValueRepository.findByCodeNameAndId(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);
            
            final List<OrderData> orderDatas=this.orderReadPlatformService.getActivePlans(clientId, null);
            
            if(!orderDatas.isEmpty()){
            	
            	 throw new ActivePlansFoundException(clientId);
            }

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is already closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } 

            if (client.isNotPending() && client.getActivationLocalDate().isAfter(closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationLocalDate());
            }

            client.close(currentUser,closureReason, closureDate.toDate());
            this.clientRepository.saveAndFlush(client);
            
            if(client.getEmail() != null){
            	final SelfCare selfCare=this.selfCareRepository.findOneByEmail(client.getEmail());
            	 if(selfCare != null){
            		 selfCare.setIsDeleted(true);
            		 this.selfCareRepository.save(selfCare);
            	 }
               final SelfCareTemporary selfCareTemporary  =this.selfCareTemporaryRepository.findOneByEmailId(client.getEmail());
               if(selfCareTemporary !=null){
            	   selfCareTemporary.delete();
            	   this.selfCareTemporaryRepository.save(selfCareTemporary);
               }
            }
            
            
            final List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CLOSE_CLIENT);
			if(actionDetaislDatas.size() != 0){
				this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(), clientId.toString(),null);
			}
			
			  ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CLOSE_CLIENT);
				
	            if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				
					this.ProvisioningWritePlatformService.postDetailsForProvisioning(clientId,Long.valueOf(0),ProvisioningApiConstants.REQUEST_TERMINATION,
							               provisionActions.getProvisioningSystem(),null);
				}
			
			
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    
    	}

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    	final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
            
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }else if (realCause.getMessage().contains("username")) {
            final String username = command.stringValueOfParameterNamed("username");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.username", "Client with username" + username
                    + "` already exists", "username", username);
        }else if (realCause.getMessage().contains("email_key")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.email", "Client with email `" + email
                    + "` already exists", "email", email);
            
        }else if (realCause.getMessage().contains("login_key")) {
            final String login = command.stringValueOfParameterNamed("login");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.login", "Client with login `" + login
                    + "` already exists", "login", login);
        }else if (realCause.getMessage().contains("unique_reference")){
        	String email = command.stringValueOfParameterNamed("email");
	          throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email.in.selfcare", "email: " + email + " already exists", "email", email);
	          
	     }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
             context.authenticatedUser();
             Configuration configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
             
             Configuration propertyConfiguration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_PROPERTY_MASTER);
             boolean isPropertyConfiguration = false;
            
            if(configuration == null){
            	throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            }
            
            isPropertyConfiguration = (propertyConfiguration == null) ? false : propertyConfiguration.isEnabled();
            
            this.fromApiJsonDeserializer.validateForCreate(command.json(),configuration.isEnabled(),isPropertyConfiguration);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);

            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);
            final Group clientParentGroup = null;
            PropertyMaster propertyMaster=null;
           
			if(propertyConfiguration != null && propertyConfiguration.isEnabled()) {		
				 propertyMaster=this.propertyMasterRepository.findoneByPropertyCode(command.stringValueOfParameterNamed("addressNo"));
				if(propertyMaster != null && propertyMaster.getClientId() != null ){
					throw new PropertyCodeAllocatedException(propertyMaster.getPropertyCode());
				}
			}

            final Client newClient = Client.createNew(clientOffice, clientParentGroup, command);
            this.clientRepository.save(newClient);
            
            final JsonElement element = fromApiJsonHelper.parse(command.json()); 
            if (fromApiJsonHelper.parameterExists("cardNumber", element)){
            	final String cardNumber = command.stringValueOfParameterNamed("cardNumber");
            	final String cardType = command.stringValueOfParameterNamed("cardType");
            	final String cvvNumber = command.stringValueOfParameterNamed("cvvNumber");
            	final String cardExpiryDate = command.stringValueOfParameterNamed("cardExpiryDate");
            	final String type = command.stringValueOfParameterNamed("type");
            	final String name = command.stringValueOfParameterNamed("email");
            	
            	JSONObject obj=new JSONObject();
    			obj.put("cardNumber", cardNumber);
    			obj.put("cardType", cardType);
    			obj.put("cvvNumber", cvvNumber);
    			obj.put("cardExpiryDate", cardExpiryDate);
    			obj.put("type",type);
    			obj.put("name", name);
    			final JsonElement eleObj = fromApiJsonHelper.parse(obj.toString());
    			
    			JsonCommand cmd=new JsonCommand(null, obj.toString(), eleObj, fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null);
    			
    			this.clientCardDetailsWritePlatformServiceJpaRepositoryImpl.addClientCardDetails(newClient.getId(), cmd);
            }
            
            final Address address = Address.fromJson(newClient.getId(),command);
			this.addressRepository.save(address);

            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory.determineClientAccountNoGenerator(newClient.getId());
                newClient.updateAccountNo(accountNoGenerator.generate());
                this.clientRepository.saveAndFlush(newClient);
            }

			if (configuration.isEnabled()) {

				final JSONObject selfcarecreation = new JSONObject();
				selfcarecreation.put("userName", newClient.getEmail());
				selfcarecreation.put("uniqueReference", newClient.getEmail());
				selfcarecreation.put("clientId", newClient.getId());
				selfcarecreation.put("device", command.stringValueOfParameterNamed("device"));
				selfcarecreation.put("mailNotification", true);
				selfcarecreation.put("password", newClient.getPassword());

				final CommandWrapper selfcareCommandRequest = new CommandWrapperBuilder().createSelfCare().withJson(selfcarecreation.toString()).build();
				this.portfolioCommandSourceWritePlatformService.logCommandSource(selfcareCommandRequest);
			}
			
			 //for property code updation with client details
				if(propertyConfiguration != null && propertyConfiguration.isEnabled()) {		
				//	PropertyMaster propertyMaster=this.propertyMasterRepository.findoneByPropertyCode(address.getAddressNo());
					if(propertyMaster !=null){
						propertyMaster.setClientId(newClient.getId());
						propertyMaster.setStatus(CodeNameConstants.CODE_PROPERTY_OCCUPIED);
					    this.propertyMasterRepository.saveAndFlush(propertyMaster);
					    PropertyTransactionHistory propertyHistory = new PropertyTransactionHistory(DateUtils.getLocalDateOfTenant(),propertyMaster.getId(),
					    		CodeNameConstants.CODE_PROPERTY_ALLOCATE,newClient.getId(),propertyMaster.getPropertyCode());
					    this.propertyHistoryRepository.save(propertyHistory);
					}
					
				}
				
            
            final List<ActionDetaislData> actionDetailsDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_CLIENT);
            if(!actionDetailsDatas.isEmpty()){
            this.actiondetailsWritePlatformService.AddNewActions(actionDetailsDatas,newClient.getId(),newClient.getId().toString(),null);
            }
            
            ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CREATE_CLIENT);
			
            if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				/*this.prepareRequestWriteplatformService.prepareRequestForRegistration(newClient.getId(),provisionActions.getAction(),
						   provisionActions.getProvisioningSystem());*/
				this.ProvisioningWritePlatformService.postDetailsForProvisioning(newClient.getId(),Long.valueOf(0),ProvisioningApiConstants.REQUEST_CLIENT_ACTIVATION,
						               provisionActions.getProvisioningSystem(),null);
			}
            
            Configuration registratFeeConfig=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_REGISTRATION_FEE);	
			if (registratFeeConfig != null&& registratFeeConfig.isEnabled()
					&& command.booleanPrimitiveValueOfParameterNamed(ClientApiConstants.registrationFlagParamName)) {
				
				final Long categoryId = command.longValueOfParameterNamed("clientCategory");
				
				FeeMasterData registrationFeeData = this.feeMasterReadplatformService.retrieveCustomerRegionClientTypeWiseFeeDetails(newClient.getId(), CodeNameConstants.CODE_REGISTRATION_FEE, categoryId);
				if (registrationFeeData != null) {
					Invoice invoice = this.invoiceOneTimeSale.calculateAdditionalFeeCharges(registrationFeeData.getChargeCode(),registrationFeeData.getId(),-1L, 
							         newClient.getId(),registrationFeeData.getDefaultFeeAmount(),BillingTransactionConstants.REGISTRATION_FEE);

					newClient.setRegistrationFee(invoice.getId());
				} 
			}
			/** 
			 * This Code is for Ticket Generation When New Client Creation
		     */
			 //createTicket(newClient.getId());
			
            this.clientRepository.saveAndFlush(newClient);
            
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(clientOffice.getId()) 
                    .withClientId(newClient.getId()).withResourceIdAsString(newClient.getId().toString())
                    .withGroupId(groupId).withEntityId(newClient.getId()).build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        } catch (JSONException e) {
        	   return CommandProcessingResult.empty();
		}
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            
            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }
            final Map<String, Object> changes = clientForUpdate.update(command);
                      
            clientForUpdate.setOffice(clientOffice);
            this.clientRepository.saveAndFlush(clientForUpdate);
            
            Configuration isSelfcareUser = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            
			if (isSelfcareUser.isEnabled()) {
				final SelfCare selfCare = selfCareRepository.findOneByClientId(clientId);
				if (selfCare != null) {
					String existingEmail = selfCare.getUniqueReference();
					String existingPassword = selfCare.getPassword();
					if(command.parameterExists("isautobilling")){
						selfCare.setIsAutoBilling(command.booleanObjectValueOfParameterNamed("isautobilling"));
					}
					if(command.parameterExists("isEnableMarketingMails")){
						if((selfCare.getIsEnableMarketingMails() =='Y' ? true:false) != ( command.booleanObjectValueOfParameterNamed("isEnableMarketingMails"))){
							changes.put("isEnableMarketingMails", command.booleanObjectValueOfParameterNamed(ClientApiConstants.isEnableMarketingMails));
						}
						selfCare.setIsEnableMarketingMails(command.booleanObjectValueOfParameterNamed("isEnableMarketingMails"));
						
					}
					
				    selfCare.update(command);
					this.selfCareRepository.saveAndFlush(selfCare);
					this.selfCareWritePlatformService.sendCredentialToProvisionSystem(selfCare,existingEmail,existingPassword, changes);
				}
			}
            
			if (changes.containsKey(ClientApiConstants.groupParamName)) {

				final List<ServiceParameters> serviceParameters = this.serviceParametersRepository.findGroupNameByclientId(clientId);
				String newGroup = null;
				if (clientForUpdate.getGroupName() != null) {
					final GroupsDetails groupsDetails = this.groupsDetailsRepository.findOne(clientForUpdate.getGroupName());
					newGroup = groupsDetails.getGroupName();
				}
				for (ServiceParameters serviceParameter : serviceParameters) {

					final Order order = this.orderRepository.findOne(serviceParameters.get(0).getOrderId());
					final Plan plan = this.planRepository.findOne(order.getPlanId());
					final String oldGroup = serviceParameter.getParameterValue();
					if (newGroup == null) {
						newGroup = plan.getPlanCode();
					}
					serviceParameter.setParameterValue(newGroup);
					this.serviceParametersRepository.saveAndFlush(serviceParameter);

					if (order.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {
						final CommandProcessingResult processingResult = this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, UserActionStatusTypeEnum.CHANGE_GROUP.toString());
						this.ProvisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(),UserActionStatusTypeEnum.CHANGE_GROUP.toString(), processingResult.resourceId(),
								oldGroup, null, order.getId(), plan.getProvisionSystem(), null,null);
					}
				}

			}
			/*if(changes.containsKey(ClientApiConstants.isEnableMarketingMails)){
				changes.put("isEnableMarketingMails", command.booleanObjectValueOfParameterNamed(ClientApiConstants.isEnableMarketingMails));
			}*/
			//hide password in activity log
			if(changes.containsKey(ClientApiConstants.passwordParamName)){
				String msg =  "Your Password Changed Successfully";
				changes.remove(ClientApiConstants.passwordParamName);
				changes.put(msg, command.stringValueOfParameterNamed(ClientApiConstants.fullnameParamName));
			}
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withOfficeId(clientForUpdate.officeId()).withClientId(clientId)
					.withEntityId(clientId).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

    @Transactional
    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            client.activate(fmt, activationDate);

            this.clientRepository.saveAndFlush(client);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final String imageName, final InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private CommandProcessingResult updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.updateImageKey(imageLocation);
        this.clientRepository.save(client);

        return new CommandProcessingResult(clientId);
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    /* (non-Javadoc)
     * @see #updateClientTaxExemption(java.lang.Long, org.obsplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override
	public CommandProcessingResult updateClientTaxExemption(final Long clientId,final JsonCommand command) {
		
		Client clientTaxStatus=null;
		
		try{
			 this.context.authenticatedUser();
			 clientTaxStatus = this.clientRepository.findOneWithNotFoundDetection(clientId);
			 char taxValue=clientTaxStatus.getTaxExemption();
			 final boolean taxStatus=command.booleanPrimitiveValueOfParameterNamed("taxExemption");
			 if(taxStatus){
				  taxValue='Y';
				  clientTaxStatus.setTaxExemption(taxValue);
			 }else{
				 taxValue='N';
				 clientTaxStatus.setTaxExemption(taxValue);
			 }
			 this.clientRepository.save(clientTaxStatus); 
			 return new CommandProcessingResultBuilder().withEntityId(clientTaxStatus.getId()).build();
		 }catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			 return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

    /* (non-Javadoc)
     * @see #updateClientBillModes(java.lang.Long, org.obsplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult updateClientBillModes(final Long clientId,final JsonCommand command) {

		Client clientBillMode=null;
	
		try{
			 this.context.authenticatedUser();
			 this.fromApiJsonDeserializer.ValidateBillMode(command);
			 clientBillMode=this.clientRepository.findOneWithNotFoundDetection(clientId);
			 final String billMode=command.stringValueOfParameterNamed("billMode");
			 if(billMode.equals(clientBillMode.getBillMode())==false){
				 clientBillMode.setBillMode(billMode);
			 }else{
				 
			 }
		 this.clientRepository.save(clientBillMode); 
		 return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				 .withEntityId(clientBillMode.getId()).build();
		}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		
	}

    /* (non-Javadoc)
     * @see #createClientParent(java.lang.Long, org.obsplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult createParentClient(final Long entityId,final JsonCommand command) {
  
			Client childClient=null;
			Client parentClient=null;
		
				try {
					this.context.authenticatedUser();
					this.fromApiJsonDeserializer.ValidateParent(command);
					final String parentAcntId=command.stringValueOfParameterNamed("accountNo");
					childClient = this.clientRepository.findOneWithNotFoundDetection(entityId);
					//count no of childs for a given client 
					final Boolean count =this.clientReadPlatformService.countChildClients(entityId);
					parentClient=this.clientRepository.findOneWithAccountId(parentAcntId);
					
						if(parentClient.getParentId() == null && !parentClient.getId().equals(childClient.getId())&&count.equals(false)){	
							childClient.setParentId(parentClient.getId());
							this.clientRepository.saveAndFlush(childClient);
						}else if(parentClient.getId().equals(childClient.getId())){
							final String errorMessage="himself can not be parent to his account.";
							throw new InvalidClientStateTransitionException("Not parent", "himself.can.not.be.parent.to his.account", errorMessage);
						}else if(count){ 
							final String errorMessage="he is already parent to some other clients";
							throw new InvalidClientStateTransitionException("Not Parent", "he.is. already. a parent.to.some other clients", errorMessage);
						}else{
							final String errorMessage="can not be parent to this account.";
							throw new InvalidClientStateTransitionException("Not parent", "can.not.be.parent.to this.account", errorMessage);
						}
						
				return new CommandProcessingResultBuilder().withEntityId(childClient.getId()).withClientId(childClient.getId()).build();
						
			  	}catch(DataIntegrityViolationException dve){
					handleDataIntegrityIssues(command, dve);
					return new CommandProcessingResult(Long.valueOf(-1));
				}
		}
	
	
	/* (non-Javadoc)
	 * @see #deleteChildFromParentClient(java.lang.Long, org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override

	public CommandProcessingResult deleteChildFromParentClient(final Long childId, final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			Client childClient = this.clientRepository.findOneWithNotFoundDetection(childId);
			final Long parentId=childClient.getParentId();
			childClient.setParentId(null);
			this.clientRepository.saveAndFlush(childClient);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(parentId).build();
	
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

	@Override
	public CommandProcessingResult createClientAdditionalInfo(JsonCommand command, Long entityId) {
            try{
                 this.context.authenticatedUser();
     				
     			final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderParamName);
     			final CodeValue gender = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_GENDER, genderId);
     				
     			final Long nationalityId = command.longValueOfParameterNamed(ClientApiConstants.nationalityParamName);
     			final CodeValue nationality = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_NATIONALITY, nationalityId);
     				
     			final Long customerTypeId = command.longValueOfParameterNamed(ClientApiConstants.idTypeParamName);
     			final CodeValue customerIdentifier = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER, customerTypeId);
     			
     			final Long prefLangId = command.longValueOfParameterNamed(ClientApiConstants.preferredLangParamName);
     			final CodeValue preferLang = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_LANG, prefLangId);
     				
     			final Long prefCommId = command.longValueOfParameterNamed(ClientApiConstants.preferredCommunicationParamName);
     			final CodeValue preferCommunication = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_COMMUNICATION, prefCommId);
     				
     			final Long ageGroupId = command.longValueOfParameterNamed(ClientApiConstants.ageGroupParamName);
     			final CodeValue ageGroup = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_AGE_GROUP, ageGroupId);
     			
     			ClientAdditionalfields clientAdditionalData = ClientAdditionalfields.fromJson(entityId,gender,nationality,customerIdentifier,preferLang,preferCommunication,ageGroup,command);
     			this.clientAdditionalFieldsRepository.save(clientAdditionalData);
     			
                 return new CommandProcessingResult(Long.valueOf(entityId));
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
    			return new CommandProcessingResult(Long.valueOf(-1));
            }
	}

	@Override
	public CommandProcessingResult updateClientAdditionalInfo(JsonCommand command) {
		try{
				
			   ClientAdditionalfields additionalfields=this.clientAdditionalFieldsRepository.findOneByClientId(command.entityId());
			   if(additionalfields == null){
				   throw new ClientAdditionalDataNotFoundException(command.entityId());
			   }
				final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderParamName);
				final CodeValue gender = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_GENDER, genderId);
					
				final Long nationalityId = command.longValueOfParameterNamed(ClientApiConstants.nationalityParamName);
				final CodeValue nationality = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_NATIONALITY, nationalityId);
					
				final Long customerTypeId = command.longValueOfParameterNamed(ClientApiConstants.idTypeParamName);
				final CodeValue customerIdentifier = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER, customerTypeId);
				
				final Long prefLangId = command.longValueOfParameterNamed(ClientApiConstants.preferredLangParamName);
				final CodeValue preferLang = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_LANG, prefLangId);
					
				final Long prefCommId = command.longValueOfParameterNamed(ClientApiConstants.preferredCommunicationParamName);
				final CodeValue preferCommunication = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_PREFER_COMMUNICATION, prefCommId);
					
				final Long ageGroupId = command.longValueOfParameterNamed(ClientApiConstants.ageGroupParamName);
				final CodeValue ageGroup = this.codeValueRepository.findByCodeNameAndId(CodeNameConstants.CODE_AGE_GROUP, ageGroupId);
				
				additionalfields.upadate(gender,nationality,customerIdentifier,preferLang,preferCommunication,ageGroup,command);
				this.clientAdditionalFieldsRepository.save(additionalfields);
				
			return new CommandProcessingResult(command.entityId());
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	@Override
	public CommandProcessingResult updateBeesmartClient(JsonCommand command) {
		try{
			
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.ValidateBeesmartUpdateClient(command);
			Client client=this.clientRepository.findOneWithAccountId(command.stringValueOfParameterNamed("accountNo"));
			
			if(client.getStatus() == 300){
				SelfCare clientUser = this.selfCareRepository.findOneByClientId(client.getId());
				if(clientUser == null){
					throw new ClientNotFoundException(client.getId());
				}
				
				final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();
				 actualChanges.put("zebraSubscriberId", command.longValueOfParameterNamed("userId"));
				 
				clientUser.setZebraSubscriberId(command.longValueOfParameterNamed("userId"));
				this.selfCareRepository.save(clientUser);
				
				return new CommandProcessingResultBuilder().withClientId(clientUser.getClientId()).with(actualChanges).build();
			}else{
				throw new PlatformDataIntegrityException("error.msg.client.status.not.active", 
						"Client status is not in Active state", client.getId(),client.getStatus());
			}
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	@Override
	public CommandProcessingResult deleteBeesmartClient(JsonCommand command,Long entityId) {
		try{
			
			this.context.authenticatedUser();
			Client client=this.clientRepository.findOneWithNotFoundDetection(entityId);
			
			if(client.getStatus() == 300){
				SelfCare clientUser = this.selfCareRepository.findOneByClientId(client.getId());
				if(clientUser == null){
					throw new ClientNotFoundException(client.getId());
				}
				
				final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>();
				 actualChanges.put("zebraSubscriberId", "null");
				 
				clientUser.setZebraSubscriberId(null);
				this.selfCareRepository.save(clientUser);
				
				return new CommandProcessingResultBuilder().withClientId(clientUser.getClientId()).with(actualChanges).build();
			}else{
				throw new PlatformDataIntegrityException("error.msg.client.status.not.active", 
						"Client status is not in Active state", client.getId(),client.getStatus());
			}
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	@Transactional
	@Override
	public CommandProcessingResult deleteCustomerNoRecord(final Long clientId, final JsonCommand command) {
		
		CommandProcessingResult  result = null;
		try {
		this.context.authenticatedUser();
		
		final List<OrderData> orderDatas=this.orderReadPlatformService.getActivePlans(clientId, null);
        
        if(!orderDatas.isEmpty()){
        	
        	 throw new ActivePlansFoundException(clientId);
        }
		
		String tables[]= {"b_client_balance", "b_client_address", "m_client_identifier", "b_clientuser", "additional_client_fields", "b_charge", "b_invoice", "b_payments", "b_deposit_refund",
				"b_order_line", "b_order_discount", "b_order_price", "b_orders_history", "b_orders", "b_association", "b_onetime_sale", "b_bill_master", "b_process_request", "m_client"};
		
		String sqlforeignkeycheck = "SET foreign_key_checks=0;";
		this.jdbcTemplate.execute(sqlforeignkeycheck);
		
		for(int i=0; i<tables.length;  i++){
			if(tables[i].equalsIgnoreCase("m_client")){
				String sql = "delete from "+tables[i]+ " where id ="+clientId;
				this.jdbcTemplate.execute(sql);
			}else if(tables[i].equalsIgnoreCase("b_order_line") || tables[i].equalsIgnoreCase("b_order_discount") ||
					tables[i].equalsIgnoreCase("b_order_price") || tables[i].equalsIgnoreCase("b_orders_history")){
				String sqlOrders = "delete from "+ tables[i] +" where order_id in (select id from b_orders where client_id="+clientId+");";
				this.jdbcTemplate.execute(sqlOrders);
			}else if(tables[i].equalsIgnoreCase("b_bill_master")){
				String sqlBillMaster = "delete bd,bm from "+ tables[i] +" bm join b_bill_details bd on bm.id = bd.Bill_id where bm.client_id="+clientId; 
				this.jdbcTemplate.execute(sqlBillMaster);
			}else if(tables[i].equalsIgnoreCase("b_process_request")){
				String sqlProcessRequest = "delete bpd,bp from "+ tables[i] +" bp join b_process_request_detail bpd on bp.id=bpd.processrequest_id where bp.client_id="+clientId; 
				this.jdbcTemplate.execute(sqlProcessRequest);
			}else{
				String sqlClient ="delete from "+tables[i]+" where client_id ="+clientId;
				this.jdbcTemplate.execute(sqlClient);
			}
		}
		result = new CommandProcessingResult(clientId,clientId);
		}catch (final SQLGrammarException e) {
			final Throwable realCause = e.getCause();
			final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
					dataValidationErrors).resource("orderdelete");
			if (realCause.getMessage().contains("Unknown table")) {
				baseDataValidator.reset().parameter("order table")
						.failWithCode("does.not.exist");
			}

			throwExceptionIfValidationWarningsExist(dataValidationErrors);
		}
		return result;
		
	}
	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
	
	/*private void createTicket(Long clientId) {
		List<ActionDetaislData> reactivationActionDetails = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_CLIENT);
		if (reactivationActionDetails.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(reactivationActionDetails, clientId,null, null);
		}
	}*/
	
	@Transactional
    @Override
    public CommandProcessingResult updateClientRequiredFields(final Long clientId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final Map<String, Object> changes = clientForUpdate.update(command);
            this.clientRepository.saveAndFlush(clientForUpdate);
            
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientId).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

}