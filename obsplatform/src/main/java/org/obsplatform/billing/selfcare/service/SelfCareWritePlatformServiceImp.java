package org.obsplatform.billing.selfcare.service;

import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.loginhistory.domain.LoginHistory;
import org.obsplatform.billing.loginhistory.domain.LoginHistoryRepository;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfCareRepository;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporary;
import org.obsplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.obsplatform.billing.selfcare.exception.SelfCareAlreadyVerifiedException;
import org.obsplatform.billing.selfcare.exception.SelfCareEmailIdDuplicateException;
import org.obsplatform.billing.selfcare.exception.SelfCareTemporaryGeneratedKeyNotFoundException;
import org.obsplatform.billing.selfcare.exception.SelfcareEmailIdNotFoundException;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.message.domain.BillingMessage;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.obsplatform.organisation.message.service.MessagePlatformEmailService;
import org.obsplatform.portfolio.client.api.ClientApiConstants;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.client.exception.ClientStatusException;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectory;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.obsplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.obsplatform.workflow.eventaction.data.OrderNotificationData;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventaction.service.EventActionReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;




@Service
public class SelfCareWritePlatformServiceImp implements SelfCareWritePlatformService{
	
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SelfCareWritePlatformServiceImp.class);
	
	private BillingMessageTemplate createSelfcareMessageDetails = null;
	private BillingMessageTemplate registerSelfcareMessageDetails = null;
	private BillingMessageTemplate newSelfcarePasswordMessageDetails = null;
	private BillingMessageTemplate createSelfcareMessageDetailsForSMS = null;
	private BillingMessageTemplate newSelfcarePasswordMessageDetailsForSMS = null;
	
	private MessagePlatformEmailService messagePlatformEmailService;
	
	private final PlatformSecurityContext context;
	private final ClientRepository clientRepository;
	private final SelfCareRepository selfCareRepository;
	private final LoginHistoryRepository loginHistoryRepository;
	private final SelfCareReadPlatformService selfCareReadPlatformService;
	private final SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer;
	private final BillingMessageRepository messageDataRepository;
	private final ConfigurationRepository configurationRepository;
	private final ProvisioningActionsRepository provisioningActionsRepository;
	private final ProcessRequestRepository processRequestRepository;
	private final EventActionReadPlatformService eventActionReadPlatformService;
	private final OrderWritePlatformService orderWritePlatformService;
	private final ClientRepositoryWrapper clientRepositorywrapper;


	@Autowired
	public SelfCareWritePlatformServiceImp(final PlatformSecurityContext context, 
			final SelfCareRepository selfCareRepository, 
		    final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer,
		    final SelfCareReadPlatformService selfCareReadPlatformService, 
			final SelfCareTemporaryRepository selfCareTemporaryRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final MessagePlatformEmailService messagePlatformEmailService,
			final ClientRepository clientRepository,
			final LoginHistoryRepository loginHistoryRepository,
			final BillingMessageRepository messageDataRepository,
			final ConfigurationRepository configurationRepository,
			final ProvisioningActionsRepository provisioningActionsRepository,
			final ProcessRequestRepository processRequestRepository,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final OrderWritePlatformService orderWritePlatformService,
			final ClientRepositoryWrapper clientRepositorywrapper) {
		
		this.context = context;
		this.selfCareRepository = selfCareRepository;
		this.selfCareCommandFromApiJsonDeserializer = selfCareCommandFromApiJsonDeserializer;
		this.selfCareReadPlatformService = selfCareReadPlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messagePlatformEmailService= messagePlatformEmailService;
		this.clientRepository=clientRepository;
		this.loginHistoryRepository=loginHistoryRepository;
		this.messageDataRepository = messageDataRepository;
		this.configurationRepository = configurationRepository;
		this.provisioningActionsRepository = provisioningActionsRepository;		
		this.processRequestRepository = processRequestRepository;
		this.eventActionReadPlatformService = eventActionReadPlatformService;
		this.orderWritePlatformService = orderWritePlatformService;
		this.clientRepositorywrapper=clientRepositorywrapper;
		
	}
	
	@Override
	public CommandProcessingResult createSelfCare(final JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			final SelfCare selfCare = SelfCare.fromJson(command);
			boolean mailnotification = command.booleanPrimitiveValueOfParameterNamed("mailNotification");
			selfCare.setIsEnableMarketingMails(true);
			if(null == selfCare.getClientId() || selfCare.getClientId()<=0L) {
				throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
			}
			
			this.selfCareRepository.save(selfCare);
			
			Client client = this.clientRepository.findOne(selfCare.getClientId());
			
			OrderNotificationData orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(client.getId(), null);
			
			Configuration isNoteForPlan = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_NOTE_FOR_PLAN);
			
			if(!isNoteForPlan.isEnabled()){
			  if (mailnotification) {

				if(null == createSelfcareMessageDetails){
					createSelfcareMessageDetails = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CREATE_SELFCARE);
				}
				
				if (createSelfcareMessageDetails != null) {
					
					String subject = createSelfcareMessageDetails.getSubject();
					String body = createSelfcareMessageDetails.getBody();
					String footer = createSelfcareMessageDetails.getFooter();
					String header = createSelfcareMessageDetails.getHeader().replace("<PARAM1>", client.getDisplayName()==null || client.getDisplayName().isEmpty()?client.getFirstname(): client.getDisplayName() + ",");
					body = body.replace("<PARAM2>", selfCare.getUserName().trim());
					body = body.replace("<PARAM3>", selfCare.getPassword().trim());

					BillingMessage billingMessage = new BillingMessage(header, body, footer, 
							orderData.getOfficeEmail(), client.getEmail(), subject,
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, createSelfcareMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

					this.messageDataRepository.save(billingMessage);

				} 
				
			}	
		 }
			//SMS Sending Details
			Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);

			if (null != configuration && configuration.isEnabled()) {

				if (null == createSelfcareMessageDetailsForSMS) {
					createSelfcareMessageDetailsForSMS = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_CREATE_SELFCARE);
				}

				if (createSelfcareMessageDetailsForSMS != null) {

					String subject = createSelfcareMessageDetailsForSMS.getSubject();
					String body = createSelfcareMessageDetailsForSMS.getBody();
					body = body.replace("<PARAM1>", selfCare.getUserName().trim());
					body = body.replace("<PARAM2>", selfCare.getPassword().trim());

					BillingMessage billingMessage = new BillingMessage(null, body, null, orderData.getOfficeEmail(),
							orderData.getClientPhone(), subject,
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, createSelfcareMessageDetailsForSMS,
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

					this.messageDataRepository.save(billingMessage);

				}
			}
			
			this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, selfCare.getClientId(), null, "Client Creation");
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		}catch(final DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	@Override
	public CommandProcessingResult createSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare = null;
		Long clientId = null;
		String ipAddress=command.stringValueOfParameterNamed("ipAddress");
		String session=command.stringValueOfParameterNamed("");
		Long loginHistoryId=null;
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreateUDPassword(command);
			selfCare = SelfCare.fromJsonODP(command);
			try{
			clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());
			if(clientId == null || clientId <= 0 ){
				throw new PlatformDataIntegrityException("client does not exist", "this user is not registered","clientId", "client is null ");
			}
			selfCare.setClientId(clientId);

			selfCareRepository.save(selfCare);
			String username=selfCare.getUserName();
			LoginHistory loginHistory=new LoginHistory(ipAddress,null,session,DateUtils.getDateOfTenant(),null,username,"ACTIVE");
    		this.loginHistoryRepository.save(loginHistory);
    		loginHistoryId=loginHistory.getId();
			}
			catch(EmptyResultDataAccessException dve){
				throw new PlatformDataIntegrityException("invalid.account.details","invalid.account.details","this user is not registered");
			}
			
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.email", "duplicate.email","duplicate.email", "duplicate.email");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(loginHistoryId).withClientId(clientId).build();
	}
		
	@Override
	public CommandProcessingResult updateSelfCareUDPassword(JsonCommand command) {
		   SelfCare selfCare=null;
		   context.authenticatedUser();
		   selfCareCommandFromApiJsonDeserializer.validateForUpdateUDPassword(command);
		   String email=command.stringValueOfParameterNamed("uniqueReference");
		   String password=command.stringValueOfParameterNamed("password");
		   selfCare=this.selfCareRepository.findOneByEmail(email);
		   if(selfCare==null){
			   throw new ClientNotFoundException(email);
		   }
		   selfCare.setPassword(password);
		   this.selfCareRepository.save(selfCare);
		   return new CommandProcessingResultBuilder().withEntityId(selfCare.getClientId()).build();
	}	
	
	@Override
	public CommandProcessingResult forgotSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare=null;
		context.authenticatedUser();
		selfCareCommandFromApiJsonDeserializer.validateForForgotUDPassword(command);
		String email=command.stringValueOfParameterNamed("uniqueReference");
		selfCare=this.selfCareRepository.findOneByEmail(email);
		if(selfCare == null){
			throw new ClientNotFoundException(email);
		}
		String password=selfCare.getPassword();
		Client client= this.clientRepository.findOne(selfCare.getClientId());
		String body="Dear "+client.getFirstname()+","+"\n"+"Your login information is mentioned below."+"\n"+"Email Id : "+email+"\n"+"Password :"+password+"\n"+"Thanks";
		String subject="Login Information";
		messagePlatformEmailService.sendGeneralMessage(email, body, subject);
		return new CommandProcessingResult(selfCare.getClientId());
	}

	
	@Override
	public CommandProcessingResult updateClientStatus(JsonCommand command,Long entityId) {
            try{
            	
            	this.context.authenticatedUser();
            	String status=command.stringValueOfParameterNamed("status");
            	SelfCare client=this.selfCareRepository.findOneByClientId(entityId);
            	if(client == null){
            		throw new ClientNotFoundException(entityId);
            	}
            	if(status.equalsIgnoreCase("ACTIVE")){
            	
            		if(status.equals(client.getStatus())){
            			throw new ClientStatusException(entityId);
            		}
            	}
            	client.setStatus(status);
            	this.selfCareRepository.save(client);
            	return new CommandProcessingResult(Long.valueOf(entityId));
            	
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
            	return new CommandProcessingResult(Long.valueOf(-1));
            }

	}

	@Override
	public CommandProcessingResult registerSelfCare(JsonCommand command) {
		
		//SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try {
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("userName");
			String returnUrl = command.stringValueOfParameterNamed("returnUrl");
			SelfCare repository = selfCareRepository.findOneByEmail(uniqueReference);
			
			if (null == repository) {

				SelfCareTemporary selfCareTemporary = SelfCareTemporary.fromJson(command);
				String unencodedPassword = RandomStringUtils.randomAlphanumeric(27);
				selfCareTemporary.setGeneratedKey(unencodedPassword);

				selfCareTemporaryRepository.save(selfCareTemporary);
				String generatedKey = selfCareTemporary.getGeneratedKey() + BillingMessageTemplateConstants.SELFCARE_REGISTRATION_CONSTANT;

				if(null == registerSelfcareMessageDetails){
					registerSelfcareMessageDetails = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SELFCARE_REGISTER);
				}
				 
				if (registerSelfcareMessageDetails != null) {
					
					String subject = registerSelfcareMessageDetails.getSubject();
					String body = registerSelfcareMessageDetails.getBody();
					String header = registerSelfcareMessageDetails.getHeader() + ",";
					String footer = registerSelfcareMessageDetails.getFooter();

					body = body.replace("<PARAM1>", returnUrl + generatedKey);
					
					BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM,
							selfCareTemporary.getUserName(), subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS,
							registerSelfcareMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

					this.messageDataRepository.save(billingMessage);

					return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(clientId).build();

				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SELFCARE_REGISTER);

			} else throw new SelfCareEmailIdDuplicateException(uniqueReference);

		} catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
	}

	@Override
	public CommandProcessingResult selfCareEmailVerification(JsonCommand command) {
	
		try{
		
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			
			String verificationKey = command.stringValueOfParameterNamed("verificationKey");
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			
			SelfCareTemporary selfCareTemporary = this.selfCareTemporaryRepository.findOneByGeneratedKey(verificationKey,uniqueReference);

			if(null == selfCareTemporary){				
				throw new SelfCareTemporaryGeneratedKeyNotFoundException(verificationKey,uniqueReference);				
			} else {
				if (selfCareTemporary.getStatus().equalsIgnoreCase("INACTIVE")
						|| selfCareTemporary.getStatus().equalsIgnoreCase("PENDING"))
					selfCareTemporary.setStatus("PENDING");
				else throw new SelfCareAlreadyVerifiedException(verificationKey);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(0L).build();
			
		}catch(final DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return null;
		}
		
	}

	@Override
	public CommandProcessingResult generateNewSelfcarePassword(JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");

			SelfCare selfCare =selfCareRepository.findOneByEmail(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{	
				
				final String existingPassword = selfCare.getPassword();
				final Map<String,Object> changes = selfCare.update(command);
				if (changes.containsKey(ClientApiConstants.passwordParamName)) {
					final Client client = this.clientRepositorywrapper.findOneWithNotFoundDetection(selfCare.getClientId());
					client.setPassword(selfCare.getPassword());
				}
				
				String generatedKey = RandomStringUtils.randomAlphabetic(10);	
				selfCare.setPassword(generatedKey);
				
				this.sendCredentialToProvisionSystem(selfCare, uniqueReference, existingPassword, changes);
				
				Client client = this.clientRepository.findOne(selfCare.getClientId());
				Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);
				
				if(null != configuration && configuration.isEnabled()) {
					
					if(null == newSelfcarePasswordMessageDetailsForSMS){
						newSelfcarePasswordMessageDetailsForSMS = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NEW_SELFCARE_PASSWORD);
					}
					
					if (newSelfcarePasswordMessageDetailsForSMS != null) {
						
						String subject = newSelfcarePasswordMessageDetailsForSMS.getSubject();
						String body = newSelfcarePasswordMessageDetailsForSMS.getBody();
						body = body.replace("<PARAM1>", selfCare.getUserName().trim());
						body = body.replace("<PARAM2>", selfCare.getPassword().trim());

						BillingMessage billingMessage = new BillingMessage(null, body, null, 
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getPhone(), subject,
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, newSelfcarePasswordMessageDetailsForSMS, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

						this.messageDataRepository.save(billingMessage);

					} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NEW_SELFCARE_PASSWORD);
				}
				
				if(null == newSelfcarePasswordMessageDetails){
					newSelfcarePasswordMessageDetails =this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NEW_SELFCARE_PASSWORD);
				}
				
				if(newSelfcarePasswordMessageDetails != null){
				String subject = newSelfcarePasswordMessageDetails.getSubject();
				String body = newSelfcarePasswordMessageDetails.getBody();
				String footer = newSelfcarePasswordMessageDetails.getFooter();
				String header = newSelfcarePasswordMessageDetails.getHeader().replace("<PARAM1>", client.getFirstname() + ",");
				body = body.replace("<PARAM2>", selfCare.getUserName().trim());
				body = body.replace("<PARAM3>", generatedKey);
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getEmail(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, newSelfcarePasswordMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);
				
				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NEW_SELFCARE_PASSWORD);
				
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		
	}

	@Override
	public CommandProcessingResult changeSelfcarePassword(final JsonCommand command) {

		try {
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForChangePassword(command);
			final String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			SelfCare selfCare = selfCareRepository.findOneByEmail(uniqueReference);
			if (selfCare == null) {
				throw new SelfcareEmailIdNotFoundException(uniqueReference);
			}
			final String existingPassword = selfCare.getPassword();
			final Map<String,Object> changes = selfCare.update(command);
			if (changes.containsKey(ClientApiConstants.passwordParamName)) {
				final Client client = this.clientRepositorywrapper.findOneWithNotFoundDetection(selfCare.getClientId());
				client.setPassword(selfCare.getPassword());
			}
			this.selfCareRepository.saveAndFlush(selfCare);
			this.sendCredentialToProvisionSystem(selfCare, uniqueReference, existingPassword, changes);
			//hide password in activity log
			if(changes.containsKey(ClientApiConstants.passwordParamName)){
				String msg = "***********";
				changes.remove(ClientApiConstants.passwordParamName);
			    changes.put(msg, command.stringValueOfParameterNamed(ClientApiConstants.fullnameParamName));
			}
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).with(changes).build();

		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);	
			return new CommandProcessingResult(Long.valueOf(-1L));
		} 
	}
	
	@Override
	public void sendCredentialToProvisionSystem(final SelfCare selfCare, final String existingUserEmail,final String existingPassword,final Map<String,Object> credentialChanges) { 

		ProvisionActions provisionActions = this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CHANGE_CREDENTIALS);

		if (provisionActions != null && 'Y' == provisionActions.getIsEnable()) {

			   try {
			    ProcessRequest processRequest = this.processRequestRepository.findOutExistsChangeCredentialsRequest(selfCare.getClientId());

			    if (processRequest != null) {
			    	
			       ProcessRequestDetails  processRequestDetail = processRequest.getProcessRequestDetails().get(0);
			       JSONObject  changeRequestJson = new JSONObject(processRequestDetail.getSentMessage());

			     changeRequestJson.put("new-email", selfCare.getUniqueReference());
			     changeRequestJson.put("old-email", existingUserEmail);
			     changeRequestJson.put("old-password", existingPassword);
			     changeRequestJson.put("new-password", selfCare.getPassword());

			     if (credentialChanges.containsKey(ClientApiConstants.firstnameParamName)) {
			      changeRequestJson.remove(ClientApiConstants.firstnameParamName);
			      changeRequestJson.put(ClientApiConstants.firstnameParamName,credentialChanges.get(ClientApiConstants.firstnameParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.lastnameParamName)) {
			      changeRequestJson.remove(ClientApiConstants.lastnameParamName);
			      changeRequestJson.put(ClientApiConstants.lastnameParamName,credentialChanges.get(ClientApiConstants.lastnameParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.phoneParamName)) {
			      changeRequestJson.remove("mobilePhone");
			      changeRequestJson.put("mobliePhone",credentialChanges.get(ClientApiConstants.phoneParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.homePhoneNumberParamName)) {
			      changeRequestJson.remove(ClientApiConstants.homePhoneNumberParamName);
			      changeRequestJson.put(ClientApiConstants.homePhoneNumberParamName,credentialChanges.get(ClientApiConstants.homePhoneNumberParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.userNameParamName)) {
			      changeRequestJson.remove(ClientApiConstants.userNameParamName);
			      changeRequestJson.put(ClientApiConstants.userNameParamName,credentialChanges.get(ClientApiConstants.userNameParamName));
			     }
			     
			     if (credentialChanges.containsKey(ClientApiConstants.isEnableMarketingMails)) {
				      changeRequestJson.remove(ClientApiConstants.isEnableMarketingMails);
				      changeRequestJson.put(ClientApiConstants.isEnableMarketingMails,credentialChanges.get(ClientApiConstants.isEnableMarketingMails));
				     }
			     
			     processRequestDetail.setSentMessage(changeRequestJson.toString());

			    } else if(!credentialChanges.isEmpty()){ 	

			     JSONObject changeSelfCareJsonForProvision = new JSONObject();

			     changeSelfCareJsonForProvision.put("obsId", selfCare.getClientId());
			     changeSelfCareJsonForProvision.put("new-email", selfCare.getUniqueReference());
			     changeSelfCareJsonForProvision.put("old-email", existingUserEmail);
			     changeSelfCareJsonForProvision.put("old-password", existingPassword);
			     changeSelfCareJsonForProvision.put("new-password", selfCare.getPassword());
			     
			     if (credentialChanges.containsKey(ClientApiConstants.firstnameParamName)) {
			      changeSelfCareJsonForProvision.put(ClientApiConstants.firstnameParamName,credentialChanges.get(ClientApiConstants.firstnameParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.lastnameParamName)) {
			      changeSelfCareJsonForProvision.put(ClientApiConstants.lastnameParamName,credentialChanges.get(ClientApiConstants.lastnameParamName));
			     }
			     if (credentialChanges.containsKey(ClientApiConstants.phoneParamName)) {
			      changeSelfCareJsonForProvision.put("mobliePhone",credentialChanges.get(ClientApiConstants.phoneParamName));
			     }

			     if (credentialChanges.containsKey(ClientApiConstants.homePhoneNumberParamName)) {
			      changeSelfCareJsonForProvision.put(ClientApiConstants.homePhoneNumberParamName,credentialChanges.get(ClientApiConstants.homePhoneNumberParamName));
			     }

			     if (credentialChanges.containsKey(ClientApiConstants.userNameParamName)) {
			      changeSelfCareJsonForProvision.put(ClientApiConstants.userNameParamName,credentialChanges.get(ClientApiConstants.userNameParamName));
			     }
			     //provision for enable marketing mails
			     if (credentialChanges.containsKey(ClientApiConstants.isEnableMarketingMails)) {
				      changeSelfCareJsonForProvision.put(ClientApiConstants.isEnableMarketingMails,credentialChanges.get(ClientApiConstants.isEnableMarketingMails));
				     }

			     processRequest = new ProcessRequest(Long.valueOf(0), selfCare.getClientId(), Long.valueOf(0),provisionActions.getProvisioningSystem(), provisionActions.getAction(), 'N', 'N');

			     ProcessRequestDetails processRequestDetail = new ProcessRequestDetails(Long.valueOf(0),Long.valueOf(0), changeSelfCareJsonForProvision.toString(), "Recieved", null,
			       DateUtils.getDateOfTenant(), null, DateUtils.getDateOfTenant(), null, 'N',provisionActions.getAction(), null);
			     processRequest.add(processRequestDetail);
			     
				} else {

					JSONObject changeSelfCareJsonForProvision = new JSONObject();

					changeSelfCareJsonForProvision.put("obsId", selfCare.getClientId());
					changeSelfCareJsonForProvision.put("new-email", selfCare.getUniqueReference());
					changeSelfCareJsonForProvision.put("old-email", existingUserEmail);
					changeSelfCareJsonForProvision.put("old-password", existingPassword);
					changeSelfCareJsonForProvision.put("new-password", selfCare.getPassword());

					if (credentialChanges.containsKey(ClientApiConstants.firstnameParamName)) {
						changeSelfCareJsonForProvision.put(ClientApiConstants.firstnameParamName, credentialChanges.get(ClientApiConstants.firstnameParamName));
					}
					if (credentialChanges.containsKey(ClientApiConstants.lastnameParamName)) {
						changeSelfCareJsonForProvision.put(ClientApiConstants.lastnameParamName, credentialChanges.get(ClientApiConstants.lastnameParamName));
					}
					if (credentialChanges.containsKey(ClientApiConstants.phoneParamName)) {
						changeSelfCareJsonForProvision.put("mobliePhone", credentialChanges.get(ClientApiConstants.phoneParamName));
					}

					if (credentialChanges.containsKey(ClientApiConstants.homePhoneNumberParamName)) {
						changeSelfCareJsonForProvision.put(ClientApiConstants.homePhoneNumberParamName, credentialChanges.get(ClientApiConstants.homePhoneNumberParamName));
					}

					if (credentialChanges.containsKey(ClientApiConstants.userNameParamName)) {
						changeSelfCareJsonForProvision.put(ClientApiConstants.userNameParamName, credentialChanges.get(ClientApiConstants.userNameParamName));
					}
					  if (credentialChanges.containsKey(ClientApiConstants.isEnableMarketingMails)) {
					      changeSelfCareJsonForProvision.put(ClientApiConstants.isEnableMarketingMails,credentialChanges.get(ClientApiConstants.isEnableMarketingMails));
					     }

					processRequest = new ProcessRequest(Long.valueOf(0), selfCare.getClientId(), Long.valueOf(0), 
							provisionActions.getProvisioningSystem(), provisionActions.getAction(), 'N', 'N');

					ProcessRequestDetails processRequestDetail = new ProcessRequestDetails(Long.valueOf(0), Long.valueOf(0), 
							changeSelfCareJsonForProvision.toString(), "Recieved", null, DateUtils.getDateOfTenant(), null, 
							DateUtils.getDateOfTenant(), null, 'N', provisionActions.getAction(), null);
					processRequest.add(processRequestDetail);
				}

                if(processRequest!=null)
			     this.processRequestRepository.save(processRequest);
			} catch (final JSONException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

	}
	
	@Override
	public void sendIsExDirectoryToProvisionSystem(final IsExDirectory isExDirectoryData, final boolean oldIsExDirectory, 
			final boolean oldIsNumberWithHeld, final boolean oldIsUmeeApp, final String serialNo, final Map<String, Object> exDirectoryChanges) {

		ProvisionActions provisionActions = this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CHANGE_EXDIRECTORY);

		if (provisionActions != null && 'Y' == provisionActions.getIsEnable()) {

			try {
				
				ProcessRequest processRequest = this.processRequestRepository.findOutExistsChangeExDirectoryRequest(isExDirectoryData.getOrderId());

				if (processRequest != null) {

					ProcessRequestDetails processRequestDetail = processRequest.getProcessRequestDetails().get(0);
					JSONObject changeRequestJson = new JSONObject(processRequestDetail.getSentMessage());

					changeRequestJson.put("orderId", isExDirectoryData.getOrderId());
					changeRequestJson.put("Tel No.", serialNo);
					
					changeRequestJson.put("new-exDirectory", isExDirectoryData.getIsExDirectory());	
					if(isExDirectoryData.getIsExDirectory() != oldIsExDirectory){
						changeRequestJson.put("old-exDirectory", oldIsExDirectory);
					}
					changeRequestJson.put("new-numberWithHeld", isExDirectoryData.getIsNumberWithHeld());
					if(isExDirectoryData.getIsNumberWithHeld() != oldIsNumberWithHeld){
						changeRequestJson.put("old-numberwithHeld", oldIsNumberWithHeld);
					}
					changeRequestJson.put("new-umeeApp", isExDirectoryData.getIsUmeeApp());
					if(isExDirectoryData.getIsUmeeApp() != oldIsUmeeApp){
						changeRequestJson.put("old-umeeApp", oldIsUmeeApp);
					}
					
					processRequestDetail.setSentMessage(changeRequestJson.toString());

				} else if (!exDirectoryChanges.isEmpty()) {

					JSONObject changeRequestJson = new JSONObject();

					changeRequestJson.put("orderId", isExDirectoryData.getOrderId());
					changeRequestJson.put("Tel No.", serialNo);
					changeRequestJson.put("new-exDirectory", isExDirectoryData.getIsExDirectory());					
					changeRequestJson.put("old-exDirectory", oldIsExDirectory);
					changeRequestJson.put("new-numberWithHeld", isExDirectoryData.getIsNumberWithHeld());
					changeRequestJson.put("old-numberwithHeld", oldIsNumberWithHeld);
					changeRequestJson.put("new-umeeApp", isExDirectoryData.getIsUmeeApp());
					changeRequestJson.put("old-umeeApp", oldIsUmeeApp);

					processRequest = new ProcessRequest(Long.valueOf(0), isExDirectoryData.getClientId(), Long.valueOf(0),
							provisionActions.getProvisioningSystem(), provisionActions.getAction(), 'N', 'N');

					ProcessRequestDetails processRequestDetail = new ProcessRequestDetails(Long.valueOf(0), Long.valueOf(0),
							changeRequestJson.toString(), "Recieved", null, DateUtils.getDateOfTenant(), null, DateUtils.getDateOfTenant(), 
							null, 'N', provisionActions.getAction(), null);
					
					processRequest.add(processRequestDetail);

				} else {

					JSONObject changeRequestJson = new JSONObject();

					changeRequestJson.put("orderId", isExDirectoryData.getOrderId());
					changeRequestJson.put("Tel No.", serialNo);
					changeRequestJson.put("new-exDirectory", isExDirectoryData.getIsExDirectory());					
					changeRequestJson.put("old-exDirectory", oldIsExDirectory);
					changeRequestJson.put("new-numberWithHeld", isExDirectoryData.getIsNumberWithHeld());
					changeRequestJson.put("old-numberwithHeld", oldIsNumberWithHeld);
					changeRequestJson.put("new-umeeApp", isExDirectoryData.getIsUmeeApp());
					changeRequestJson.put("old-umeeApp", oldIsUmeeApp);

					processRequest = new ProcessRequest(Long.valueOf(0), isExDirectoryData.getClientId(), Long.valueOf(0),
							provisionActions.getProvisioningSystem(),
							provisionActions.getAction(), 'N', 'N');

					ProcessRequestDetails processRequestDetail = new ProcessRequestDetails(Long.valueOf(0), Long.valueOf(0), 
							changeRequestJson.toString(), "Recieved", null, DateUtils.getDateOfTenant(), null, DateUtils.getDateOfTenant(), 
							null, 'N', provisionActions.getAction(), null);
					
					processRequest.add(processRequestDetail);
				}
				processRequest.setOrderId(isExDirectoryData.getOrderId());

				if (processRequest != null)
					this.processRequestRepository.save(processRequest);
			} catch (final JSONException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

	}
	
	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
		
		final Throwable realCause = dve.getMostSpecificCause();
		
		if (realCause.getMessage().contains("username") && command.stringValueOfParameterNamed("uniqueReference").equalsIgnoreCase(command.stringValueOfParameterNamed("userName"))) {
			
			throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email","email: " + 
			command.stringValueOfParameterNamed("uniqueReference") + " already exists", "email",command.stringValueOfParameterNamed("uniqueReference"));

		} else if (realCause.getMessage().contains("unique_reference")) {
			
			throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email","email: " + 
		    command.stringValueOfParameterNamed("uniqueReference") + " already exists", "email",command.stringValueOfParameterNamed("uniqueReference"));

		} else if (realCause.getMessage().contains("username")) {

			throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.userName","User Name: " + 
			command.stringValueOfParameterNamed("userName") + " already exists", "userName",command.stringValueOfParameterNamed("userName"));
		}

		logger.error(dve.getMessage(), dve);

		throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue","Unknown data integrity issue with resource.");
	}

}