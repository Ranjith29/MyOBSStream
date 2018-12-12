package org.obsplatform.crm.clientprospect.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.domain.ClientProspect;
import org.obsplatform.crm.clientprospect.domain.ClientProspectJpaRepository;
import org.obsplatform.crm.clientprospect.domain.ProspectCardDetails;
import org.obsplatform.crm.clientprospect.domain.ProspectCardDetailsJpaRepository;
import org.obsplatform.crm.clientprospect.domain.ProspectDetail;
import org.obsplatform.crm.clientprospect.domain.ProspectDetailJpaRepository;
import org.obsplatform.crm.clientprospect.domain.ProspectOrder;
import org.obsplatform.crm.clientprospect.domain.ProspectOrderJpaRepository;
import org.obsplatform.crm.clientprospect.domain.ProspectPayment;
import org.obsplatform.crm.clientprospect.domain.ProspectPaymentJpaRepository;
import org.obsplatform.crm.clientprospect.serialization.ClientProspectCommandFromApiJsonDeserializer;
import org.obsplatform.finance.paymentsgateway.api.PaymentGatewayApiResource;
import org.obsplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class ClientProspectWritePlatformServiceImp implements ClientProspectWritePlatformService {

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(ClientProspectWritePlatformServiceImp.class);

	private final PlatformSecurityContext context;
	private final ClientProspectJpaRepository clientProspectJpaRepository;
	private final ProspectDetailJpaRepository prospectDetailJpaRepository;
	private final ClientProspectCommandFromApiJsonDeserializer clientProspectCommandFromApiJsonDeserializer;
	private final FromJsonHelper fromApiJsonHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ProspectOrderJpaRepository prospectOrderJpaRepository;
	private final ClientReadPlatformService clientReadPlatformService;
	private final PaymentGatewayApiResource paymentGatewayApiResource;
	/*private final ClientWritePlatformService clientWritePlatformService;*/
	private final OrderWritePlatformService orderWritePlatformService;
	private final ConfigurationRepository repository;
	private final ProspectPaymentJpaRepository prospectPaymentJpaRepository;
	private final ProspectCardDetailsJpaRepository prospectCardDetailsJpaRepository;

	@Autowired
	public ClientProspectWritePlatformServiceImp(
			final PlatformSecurityContext context,
			final ClientProspectJpaRepository clientProspectJpaRepository,
			final ClientProspectCommandFromApiJsonDeserializer clientProspectCommandFromApiJsonDeserializer,
			final FromJsonHelper fromApiJsonHelper,
			final ProspectDetailJpaRepository prospectDetailJpaRepository,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ProspectOrderJpaRepository prospectOrderJpaRepository,
			final ClientReadPlatformService clientReadPlatformService,
			final PaymentGatewayApiResource paymentGatewayApiResource,
			/*final ClientWritePlatformService clientWritePlatformService,*/
			final OrderWritePlatformService orderWritePlatformService,
			final ConfigurationRepository repository,
			final ProspectPaymentJpaRepository prospectPaymentJpaRepository,
			final ProspectCardDetailsJpaRepository prospectCardDetailsJpaRepository) {
		this.context = context;
		this.clientProspectJpaRepository = clientProspectJpaRepository;
		this.clientProspectCommandFromApiJsonDeserializer = clientProspectCommandFromApiJsonDeserializer;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.prospectDetailJpaRepository = prospectDetailJpaRepository;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.prospectOrderJpaRepository = prospectOrderJpaRepository;
		this.clientReadPlatformService = clientReadPlatformService;
		this.paymentGatewayApiResource = paymentGatewayApiResource;
		/*this.clientWritePlatformService = clientWritePlatformService;*/
		this.orderWritePlatformService = orderWritePlatformService;
		this.repository = repository;
		this.prospectPaymentJpaRepository = prospectPaymentJpaRepository;
		this.prospectCardDetailsJpaRepository = prospectCardDetailsJpaRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult createProspect(JsonCommand command) {

		try {
			context.authenticatedUser();
			this.clientProspectCommandFromApiJsonDeserializer.validateForCreate(command.json());
			
			final ClientProspect entity = ClientProspect.fromJson(fromApiJsonHelper, command);
			this.clientProspectJpaRepository.save(entity);
			
			final JsonElement element = fromApiJsonHelper.parse(command.json()); 
			if (fromApiJsonHelper.parameterExists("planCode", element)) {
				Long planCode = command.longValueOfParameterNamed("planCode");
				Long contractPeriod = command.longValueOfParameterNamed("contractPeriod");
				String paytermCode = command.stringValueOfParameterNamed("paytermCode");
				Long noOfConnections = command.longValueOfParameterNamed("noOfConnections");
				BigDecimal totalPrice = command.bigDecimalValueOfParameterNamed("totalPrice");
				ProspectOrder prospectOrder = new ProspectOrder(entity.getId(), planCode, contractPeriod, paytermCode, noOfConnections, totalPrice);
				this.prospectOrderJpaRepository.save(prospectOrder);
				
				//adding client card details when creating prospect 
				
				final String isPrepaid = command.stringValueOfParameterNamed("isPrepaid");
				final String cardNumber = command.stringValueOfParameterNamed("cardNumber");
				
				if (!isPrepaid.isEmpty() && !cardNumber.isEmpty()) {

					//final String cardNumber = command.stringValueOfParameterNamed("cardNumber");
					final String cardType = command.stringValueOfParameterNamed("cardType");
					final String cvvNumber = command.stringValueOfParameterNamed("cvvNumber");
					final String expiryDate = command.stringValueOfParameterNamed("cardExpiryDate");
					final String[] expiryDateParts = expiryDate.split("/");
					final String cardExpiryDate = expiryDateParts[1] + expiryDateParts[0];
					final String name = command.stringValueOfParameterNamed("name");
					final String type = command.stringValueOfParameterNamed("type");
					ProspectCardDetails prospectCardDetails = new ProspectCardDetails(entity.getId(), prospectOrder.getId(), cardNumber,
							cardType, cvvNumber, cardExpiryDate, name, type);
					this.prospectCardDetailsJpaRepository.save(prospectCardDetails);
				}
			}

			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(entity.getId()).build();
			
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (ParseException pe) {
			throw new PlatformDataIntegrityException(
					"invalid.date.and.time.format",
					"invalid.date.and.time.format",
					"invalid.date.and.time.format");
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult followUpProspect(final JsonCommand command, final Long prospectId) {
		try {
			context.authenticatedUser();
			this.clientProspectCommandFromApiJsonDeserializer.validateForUpdate(command.json());
			
			final ProspectDetail prospectDetail = ProspectDetail.fromJson(command, prospectId);
			prospectDetailJpaRepository.save(prospectDetail);
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(prospectDetail.getProspectId()).build();
		
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		} catch (ParseException e) {
			throw new PlatformDataIntegrityException(
					"invalid.date.and.time.format",
					"invalid.date.and.time.format",
					"invalid.date.and.time.format");
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult deleteProspect(JsonCommand command) {
		
		context.authenticatedUser();
		final ClientProspect clientProspect = retrieveCodeBy(command.entityId());
		clientProspect.setIsDeleted('Y');
		clientProspect.setStatus("Canceled");
		clientProspect.setStatusRemark(command.stringValueOfParameterNamed("statusRemark"));
		
		this.clientProspectJpaRepository.saveAndFlush(clientProspect);
		
		return new CommandProcessingResultBuilder().withEntityId(
				clientProspect.getId()).build();
	}

	private ClientProspect retrieveCodeBy(final Long prospectId) {
		
		final ClientProspect clientProspect = this.clientProspectJpaRepository.findOne(prospectId);
		
		if (clientProspect == null) {
			throw new CodeNotFoundException(prospectId.toString());
		}
		
		return clientProspect;
	}

	@Override
	public CommandProcessingResult convertToClient(final Long entityId) {

		final AppUser currentUser = context.authenticatedUser();
		final ClientProspect clientProspect = retrieveCodeBy(entityId);
		final ProspectOrder prospectOrder = retrieveProspectOrderBy(entityId);
		final ProspectCardDetails prospectCardDetails = retriveProspectCardDetailsBy(entityId);

		Long clientId = null;

		final JSONObject newClientJsonObject = new JSONObject();
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		try {
			
			String activationDate = formatter.format(DateUtils.getDateOfTenant());

			final Long officeId = currentUser.getOffice().getId();
			newClientJsonObject.put("dateFormat", "dd MMMM yyyy");
			newClientJsonObject.put("locale", "en");
			newClientJsonObject.put("officeId", officeId);
			newClientJsonObject.put("firstname", clientProspect.getFirstName());
			newClientJsonObject.put("middlename", clientProspect.getMiddleName());
			newClientJsonObject.put("lastname", clientProspect.getLastName());
			newClientJsonObject.put("fullname", "");
			newClientJsonObject.put("externalId", "");
			newClientJsonObject.put("clientCategory", "20");
			// newClientJsonObject.put("active","300");
			newClientJsonObject.put("activationDate", activationDate);
			newClientJsonObject.put("active", "true");
			newClientJsonObject.put("email", clientProspect.getEmail());
			newClientJsonObject.put("phone", clientProspect.getMobileNumber());
			newClientJsonObject.put("flag", false);
			/*
			 * newClientJsonObject.put("login","");
			 * newClientJsonObject.put("password","");
			 */

			newClientJsonObject.put("addressNo", clientProspect.getAddress());
			newClientJsonObject.put("street", clientProspect.getStreetArea());
			newClientJsonObject.put("city", clientProspect.getCityDistrict());
			newClientJsonObject.put("zipCode", clientProspect.getZipCode());
			newClientJsonObject.put("state", clientProspect.getState());
			newClientJsonObject.put("country", clientProspect.getCountry());
			newClientJsonObject.put("flag", "false");
			
			//adding client card details 
			if (prospectCardDetails != null) {

				newClientJsonObject.put("cardNumber", prospectCardDetails.getCardNumber());
				newClientJsonObject.put("cardType", prospectCardDetails.getCardType());
				newClientJsonObject.put("cvvNumber", prospectCardDetails.getCvvNumber());
				newClientJsonObject.put("cardExpiryDate", prospectCardDetails.getCardExpiryDate());
				newClientJsonObject.put("type", prospectCardDetails.getType());
			}

			final CommandWrapper commandNewClient = new CommandWrapperBuilder().createClient()
					.withJson(newClientJsonObject.toString().toString()).build(); //
			
			final CommandProcessingResult clientResult = this.commandsSourceWritePlatformService.logCommandSource(commandNewClient);
			/*
			 * final CommandWrapper commandRequest = new
			 * CommandWrapperBuilder().
			 * createAddress(clientResult.getClientId()).
			 * withJson(newClientAddressObject.toString().toString()).build();
			 * final CommandProcessingResult addressResult =
			 * this.commandsSourceWritePlatformService
			 * .logCommandSource(commandRequest);
			 */

			clientProspect.setStatusRemark(clientResult.getClientId().toString());
			clientId = clientResult.getClientId();

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		 final Configuration config = this.repository.findOneByName("prospect-payment");
		 if(!config.isEnabled()){
			 
			 if(null != prospectOrder){
					
					for(int i=1; i<=prospectOrder.getNoOfConnections();i++){
						
						final JSONObject object = new JSONObject();
						try {
							object.put("billAlign", "false");
							object.put("planCode", prospectOrder.getPlanId());
							object.put("contractPeriod", prospectOrder.getContarctPeriod());
							object.put("paytermCode", prospectOrder.getPaytermCode());
							object.put("locale", "en");
							object.put("isNewplan", "true");
							object.put("dateFormat", "dd MMMM yyyy");
							object.put("start_date", formatter.format(DateUtils.getDateOfTenant()));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						final CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(clientId).withJson(object.toString()).build();
				        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					}
					
				}
		 }
		
		clientProspect.setStatus("Closed");
		// clientProspect.setIsDeleted('Y');

		// clientProspect.setStatusRemark(command.stringValueOfParameterNamed("statusRemark"));
		
		this.clientProspectJpaRepository.saveAndFlush(clientProspect);
		
		return new CommandProcessingResultBuilder().withEntityId(clientId).build();

	}

	private ProspectCardDetails retriveProspectCardDetailsBy(final Long prospectId) {
		
		final ProspectCardDetails prospectCardDetails = this.prospectCardDetailsJpaRepository.findOneProspectCardDetailsByProspectID(prospectId);
		return prospectCardDetails;
	}

	@Override
	public CommandProcessingResult updateProspect(JsonCommand command) {
		
		try {
			context.authenticatedUser();
			this.clientProspectCommandFromApiJsonDeserializer.validateForCreate(command.json());

			final ClientProspect pros = retrieveCodeBy(command.entityId());
			final Map<String, Object> changes = pros.update(command);

			if (!changes.isEmpty()) {
				this.clientProspectJpaRepository.save(pros);
			}
			
			
			final ProspectOrder prosOrder = retrieveProspectOrderBy(pros.getId());
			if(null != prosOrder){
				final Map<String, Object> changesProsOrder = prosOrder.update(command);
				if(!changesProsOrder.isEmpty()){
					this.prospectOrderJpaRepository.save(prosOrder);
				}
			}
			
			final ProspectCardDetails prospectCardDetails = retriveProspectCardDetailsBy(pros.getId());
			
			if(prospectCardDetails != null){
				final Map<String, Object> changesProspectCardDetails = prospectCardDetails.update(command);
				if(!changesProspectCardDetails.isEmpty()){
					this.prospectCardDetailsJpaRepository.save(prospectCardDetails);
				}
			}
			
			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withEntityId(pros.getId()) //
					.with(changes) //
					.build();
			
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
		}
		return new CommandProcessingResultBuilder().withEntityId(-1L).build();
	}

	private void handleDataIntegrityIssues(final JsonCommand element,
			final DataIntegrityViolationException dve) {

		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("serial_no_constraint")) {
			throw new PlatformDataIntegrityException(
					"validation.error.msg.inventory.item.duplicate.serialNumber",
					"validation.error.msg.inventory.item.duplicate.serialNumber",
					"validation.error.msg.inventory.item.duplicate.serialNumber",
					"");
		}

		LOGGER.error(dve.getMessage(), dve);
	}
	
	private ProspectOrder retrieveProspectOrderBy(final Long prospectId) {
		
		final ProspectOrder prospectOrder = this.prospectOrderJpaRepository.findOneProspectOrderByProspectId(prospectId);
		
		return prospectOrder;
	}

	@Transactional
	@Override
	public CommandProcessingResult createPaymentProspect(JsonCommand command, final Long clientId) {
		
		//JSONObject fromProspect = new JSONObject(jsonRequestBody);
		String locale = command.stringValueOfParameterNamed("locale");
		/*String firstName = command.stringValueOfParameterNamed("firstName");
		String middleName = command.stringValueOfParameterNamed("middleName");
		String lastName = command.stringValueOfParameterNamed("lastName");
		String email = command.stringValueOfParameterNamed("email");
		String mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
		String address = command.stringValueOfParameterNamed("address");
		
		String streetArea = command.stringValueOfParameterNamed("streetArea");
		String cityDistrict = command.stringValueOfParameterNamed("cityDistrict");
		String zipCode = command.stringValueOfParameterNamed("zipCode");
		String state = command.stringValueOfParameterNamed("state");
		String country = command.stringValueOfParameterNamed("country");*/
		String email = command.stringValueOfParameterNamed("email");
		String cardNumber = command.stringValueOfParameterNamed("cardNumber");
		String cardType = command.stringValueOfParameterNamed("cardType");
		String total_amount = command.stringValueOfParameterNamed("total_amount");
		String transactionId = command.stringValueOfParameterNamed("transactionId");
		String dateFormat = command.stringValueOfParameterNamed("dateFormat");
		
		Long noOfConnections = command.longValueOfParameterNamed("noOfConnections");
		Long planCode = command.longValueOfParameterNamed("planCode");
		String paytermCode = command.stringValueOfParameterNamed("paytermCode");
		Long contractPeriod = command.longValueOfParameterNamed("contractPeriod");
		
		final AppUser currentUser = context.authenticatedUser();
		final JSONObject newClientJsonObject = new JSONObject();
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		String activationDate = formatter.format(DateUtils.getDateOfTenant());
		CommandProcessingResult result = null;
		/*final Long officeId = currentUser.getOffice().getId();
		CommandProcessingResult result;
		try {
			newClientJsonObject.put("dateFormat", "dd MMMM yyyy");
			newClientJsonObject.put("locale", locale);
			newClientJsonObject.put("officeId", officeId);
			newClientJsonObject.put("firstname", firstName);
			if(command.hasParameter("middleName")){
				newClientJsonObject.put("middlename", middleName);
			}
			newClientJsonObject.put("lastname", lastName);
			newClientJsonObject.put("fullname", "");
			newClientJsonObject.put("externalId", "");
			newClientJsonObject.put("clientCategory", "20");
			newClientJsonObject.put("activationDate", activationDate);
			newClientJsonObject.put("active", "true");
			newClientJsonObject.put("email", email);
			newClientJsonObject.put("phone", mobileNumber);
			newClientJsonObject.put("flag", false);
			
			newClientJsonObject.put("addressNo", address);
			if(command.hasParameter("streetArea")){
				newClientJsonObject.put("middlename", streetArea);
			}
			newClientJsonObject.put("city", cityDistrict);
			if(command.hasParameter("zipCode")){
				newClientJsonObject.put("middlename", zipCode);
			}
			newClientJsonObject.put("state", state);
			newClientJsonObject.put("country", country);
			newClientJsonObject.put("flag", "false");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		final JsonElement element = fromApiJsonHelper.parse(newClientJsonObject.toString());
		JsonCommand changeCommandCommand = new JsonCommand(null,newClientJsonObject.toString(), element, fromApiJsonHelper,
				null, null, null, null, null, null, null, null, null, null, 
				null, null);
		
		result = this.clientWritePlatformService.createClient(changeCommandCommand);*/
		
		ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
		JSONObject postJsonObj = new JSONObject();
		
		try {
			postJsonObj.put("cardNumber", cardNumber);
			postJsonObj.put("cardType", cardType);
			postJsonObj.put("clientId", clientId);
			if(clientData.getCurrency() != null){
				postJsonObj.put("currency", clientData.getCurrency());
			}else{
				postJsonObj.put("currency", "USD");
			}
			postJsonObj.put("emailId", email);
			postJsonObj.put("locale", locale);
			postJsonObj.put("source", "authorizenet");
			postJsonObj.put("total_amount", total_amount);
			postJsonObj.put("transactionId", transactionId);
			postJsonObj.put("dateFormat", dateFormat);
			
			JSONObject otherData = new JSONObject();
			otherData.put("paymentDate", formatter.format(DateUtils.getDateOfTenant()));
			otherData.put("email", email);
			if(clientData.getCurrency() != null){
				otherData.put("currency", clientData.getCurrency());
			}else{
				otherData.put("currency", "USD");
			}
			otherData.put("transactionId", transactionId);
			postJsonObj.put("otherData", otherData);
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		String onlinePGData = this.paymentGatewayApiResource.OnlinePaymentMethod(postJsonObj.toString());
		JSONObject onlinePGDataJson = null;
		Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		actualChanges.put("onlinePGData", onlinePGData);
		
		for(int i=1; i<=noOfConnections;i++){
			
			final JSONObject object = new JSONObject();
			try {
				object.put("billAlign", "false");
				object.put("planCode", planCode);
				object.put("paytermCode", paytermCode);
				object.put("locale", locale);
				object.put("isNewplan", "true");
				object.put("dateFormat", "dd MMMM yyyy");
				object.put("start_date", formatter.format(DateUtils.getDateOfTenant()));
				object.put("contractPeriod", contractPeriod);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			final JsonElement elementOrder = fromApiJsonHelper.parse(object.toString());
			JsonCommand changeCommandCommandOrder = new JsonCommand(null,object.toString(), elementOrder, fromApiJsonHelper,
					null, null, null, null, null, null, null, null, null, null, 
					null, null);
			
			result = this.orderWritePlatformService.createOrder(clientId, changeCommandCommandOrder);
			
			if(null != result && result.resourceId() <= 1){
				
				ProspectPayment pp = new ProspectPayment(clientId, command.toString(), 'Y', "");
				this.prospectPaymentJpaRepository.save(pp);
			}
			ProspectPayment checkPP = this.prospectPaymentJpaRepository.findOneProspectPaymentByClientId(clientId);
			if(null != checkPP && null != result && result.resourceId() >= 1){
				checkPP.setIsProcessedObs('Y');
				this.prospectPaymentJpaRepository.save(checkPP);
			}
	        try {
	        	
	        	onlinePGDataJson = new JSONObject(onlinePGData);
				onlinePGDataJson.put("orderId", result.resourceId());
				onlinePGDataJson.put("clientId", clientId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        
		}
		
		return result.withChanges(result.resourceId(), actualChanges);
	}

}
