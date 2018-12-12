package org.obsplatform.billing.selfcare.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.loginhistory.domain.LoginHistory;
import org.obsplatform.billing.loginhistory.domain.LoginHistoryRepository;
import org.obsplatform.billing.loginhistory.service.LoginHistoryReadPlatformService;
import org.obsplatform.billing.selfcare.data.SelfCareData;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfCareRepository;
import org.obsplatform.billing.selfcare.exception.SelfCareNotFoundException;
import org.obsplatform.billing.selfcare.service.ExceededNumberOfViewersException;
import org.obsplatform.billing.selfcare.service.SelfCareReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.obsplatform.finance.billingmaster.service.BillMasterReadPlatformService;
import org.obsplatform.finance.billingorder.data.BillDetailsData;
import org.obsplatform.finance.clientbalance.data.ClientBalanceData;
import org.obsplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.obsplatform.finance.payments.data.PaymentData;
import org.obsplatform.finance.payments.service.PaymentReadPlatformService;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("selfcare")
@Component
@Scope("singleton")
public class SelfCareApiResource {

	private PlatformSecurityContext context;
	private final String resourceNameForPermissions = "SELFCARE";
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<SelfCareData> toApiJsonSerializerForItem;
	private final SelfCareReadPlatformService selfCareReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;
	private final AddressReadPlatformService addressReadPlatformService;
	private final ClientBalanceReadPlatformService clientBalanceReadPlatformService;
	private final OrderReadPlatformService orderReadPlatformService;
	private final BillMasterReadPlatformService billMasterReadPlatformService;
	private final PaymentReadPlatformService paymentReadPlatformService;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private final ConfigurationRepository configurationRepository;
	private final SelfCareRepository selfCareRepository;
	private final LoginHistoryReadPlatformService loginHistoryReadPlatformService;
	private final LoginHistoryRepository loginHistoryRepository;
	private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
	private final FromJsonHelper fromJsonHelper;

	@Autowired
	public SelfCareApiResource(final PlatformSecurityContext context,final SelfCareRepository selfCareRepository,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,final LoginHistoryRepository loginHistoryRepository, 
			final DefaultToApiJsonSerializer<SelfCareData> toApiJsonSerializerForItem,final AddressReadPlatformService addressReadPlatformService,  
			final SelfCareReadPlatformService selfCareReadPlatformService,final PaymentReadPlatformService paymentReadPlatformService,  
			final ClientBalanceReadPlatformService balanceReadPlatformService, final ClientReadPlatformService clientReadPlatformService, 
			final OrderReadPlatformService  orderReadPlatformService, final BillMasterReadPlatformService billMasterReadPlatformService,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,final ConfigurationRepository configurationRepository,
			final LoginHistoryReadPlatformService loginHistoryReadPlatformService,
			final PaymentGatewayConfigurationRepositoryWrapper gatewayConfigurationRepositoryWrapper,
			final FromJsonHelper fromJsonHelper) {
		
				this.context = context;
				this.commandsSourceWritePlatformService = commandSourceWritePlatformService;
				this.toApiJsonSerializerForItem = toApiJsonSerializerForItem;
				this.selfCareReadPlatformService = selfCareReadPlatformService;
				this.paymentReadPlatformService = paymentReadPlatformService;
				this.addressReadPlatformService = addressReadPlatformService;
				this.paymentGatewayConfigurationRepositoryWrapper = gatewayConfigurationRepositoryWrapper;
				this.clientBalanceReadPlatformService = balanceReadPlatformService;
				this.clientReadPlatformService = clientReadPlatformService;
				this.orderReadPlatformService = orderReadPlatformService;
				this.billMasterReadPlatformService = billMasterReadPlatformService;
				this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
				this.configurationRepository=configurationRepository;
				this.selfCareRepository=selfCareRepository;
				this.loginHistoryReadPlatformService=loginHistoryReadPlatformService;
				this.loginHistoryRepository=loginHistoryRepository;
				this.fromJsonHelper = fromJsonHelper;
	}
	

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createSelfCareClient(final String jsonRequestBody) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createSelfCare().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializerForItem.serialize(result);	
	}

	
	@POST
	@Path("password")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createSelfCareClientUDPassword(final String jsonRequestBody,@Context HttpServletRequest request) throws JSONException {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		JSONObject json = new JSONObject(jsonRequestBody); 
		json.put("session", request.getSession().getId());
		json.put("ipAddress", request.getRemoteHost());
		String jsonRequestBody1=json.toString();		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createSelfCareUDP().withJson(jsonRequestBody1).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			
	    return this.toApiJsonSerializerForItem.serialize(result);	
	}	
	
	private void viewerChecking(final String username, final Long clientId) {
		
		final Configuration viewers = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IS_ACTIVE_VIEWERS);
		
		if (viewers != null && viewers.isEnabled()) {		
			int maxViewersAllowed = Integer.parseInt(viewers.getValue());
			int activeUsers = this.loginHistoryReadPlatformService.retrieveNumberOfUsers(username);
			//int activeUsers=this.ownedHardwareReadPlatformService.retrieveNoOfActiveUsers(clientId);		
			/** Condition for checking the number of active users  */
			if (activeUsers >= maxViewersAllowed) {
				throw new ExceededNumberOfViewersException(clientId);
			}
		}
	}
   
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String logIn(@QueryParam("username") final String username, @QueryParam("password") final String password, 
			@QueryParam("serialNo") final String serialNo, @Context final HttpServletRequest request) {

		try {
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
			final SelfCareData selfcare = this.selfCareReadPlatformService.login(username, password);
			SelfCareData careData = new SelfCareData();
			if (selfcare != null && selfcare.getPassword().equals(password) && selfcare.getClientId() > 0) {
				
				final Long clientId = selfcare.getClientId();
				/** @see SelfCareApiResource#viewerChecking(String username, Long clientId)  */
				viewerChecking(username, clientId);
				
				final PaymentGatewayConfiguration paypalConfigData = this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK);
				careData.setPaypalConfigData(paypalConfigData);

				
				String ipAddress = request.getRemoteHost();
				String sessionId = request.getSession().getId();
				Long loginHistoryId = null;
				careData.setClientId(clientId);
				// if(request.getSession().isNew()){
				LoginHistory loginHistory = new LoginHistory(ipAddress, serialNo, sessionId, DateUtils.getDateOfTenant(), null, username, "ACTIVE");
				this.loginHistoryRepository.save(loginHistory);
				loginHistoryId = loginHistory.getId();
				// }
				ClientData clientsData = this.clientReadPlatformService.retrieveOne(clientId);
				ClientBalanceData balanceData = this.clientBalanceReadPlatformService.retrieveBalance(clientId);
				List<AddressData> addressData = this.addressReadPlatformService.retrieveAddressDetailsBy(clientId,null);
				final List<OrderData> clientOrdersData = this.orderReadPlatformService.retrieveClientOrderDetails(clientId);
				final SearchSqlQuery searchCodes = SearchSqlQuery.forSearch(null, null, null);
				
				final Page<BillDetailsData> statementsData = this.billMasterReadPlatformService.retrieveStatments(searchCodes, clientId);
				List<PaymentData> paymentsData = paymentReadPlatformService.retrivePaymentsData(clientId);
				final List<TicketMasterData> ticketMastersData = this.ticketMasterReadPlatformService.retrieveClientTicketDetails(clientId);
				careData.setDetails(clientsData, balanceData, addressData, clientOrdersData, statementsData,paymentsData, ticketMastersData, loginHistoryId);

				Configuration balanceCheck = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_BALANCE_CHECK);
				clientsData.setBalanceCheck(balanceCheck.isEnabled());

				PaymentGatewayConfiguration paypalConfigDataForIos = this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK);
				careData.setPaypalConfigDataForIos(paypalConfigDataForIos);
				return this.toApiJsonSerializerForItem.serialize(careData);
			} else {
				MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
				throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
			}
		} catch (EmptyResultDataAccessException e) {
			throw new PlatformDataIntegrityException("result.set.is.null", "result.set.is.null", "result.set.is.null");
		}

	}
		  
    @PUT
    @Path("status/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoginStatus(@PathParam("clientId")Long clientId,final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientStatus(clientId).withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializerForItem.serialize(result);
    }
    
    @POST
    @Path("register")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String registerSelfCare(final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().registerSelfCareRegister().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializerForItem.serialize(result);	
	    
	}
    
    @PUT
    @Path("register")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String SelfCareEmailVerication(final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().SelfCareEmailVerification().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializerForItem.serialize(result);	
	    
	}
    
    @PUT
    @Path("forgotpassword")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewSelfCarePassword(final String jsonRequestBody) {
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createNewSelfCarePassword().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializerForItem.serialize(result);	
	    
	}
    
    @PUT
    @Path("resetpassword")
    @Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateSelfCareUDPassword(final String apiRequestBodyAsJson){
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSelfCareUDPassword().withJson(apiRequestBodyAsJson).build(); //
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    	return this.toApiJsonSerializerForItem.serialize(result);
    	 /*	SelfCareData careData = new SelfCareData(email,password);
            Long clientId=this.selfCareWritePlatformService.updateSelfCareUDPassword(careData);
			return this.toApiJsonSerializerForItem.serialize(CommandProcessingResult.resourceResult(clientId, null));*/
        	  	
    }
   
	@POST
	@Path("/forgotpassword")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public String forgotSelfCareUDPassword(final String apiRequestBodyAsJson){
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().forgetSelfCareUDPassword().withJson(apiRequestBodyAsJson).build(); //
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializerForItem.serialize(result);
   	
    }
    
    @PUT
    @Path("changepassword")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String selfCareChangePassword(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSelfcarePassword().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializerForItem.serialize(result);	
	    
	}
    
    @PUT
    @Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String selfCareKortaTokenStore(@PathParam("clientId") Long clientId, final String jsonRequestBody) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		final JsonElement element = this.fromJsonHelper.parse(jsonRequestBody);
		
		SelfCare selfcare = this.selfCareRepository.findOneByClientId(clientId);
		
		if (selfcare == null) {
			throw new SelfCareNotFoundException(clientId);	
		} 
		
		if(this.fromJsonHelper.parameterExists("kortaToken", element)) {
			String token = this.fromJsonHelper.extractStringNamed("kortaToken", element);
			selfcare.setToken(token);
		}
		
		if(this.fromJsonHelper.parameterExists("isautobilling", element)) {
			Boolean isAutoBilling = this.fromJsonHelper.extractBooleanNamed("isautobilling", element);
			selfcare.setIsAutoBilling(isAutoBilling);
		}
		//isWorldpay
		if(this.fromJsonHelper.parameterExists("isWorldpayBilling", element)) {
			Boolean isWorldpayBilling = this.fromJsonHelper.extractBooleanNamed("isWorldpayBilling", element);
			selfcare.setIsWorldpayBilling(isWorldpayBilling);
		}
		
		if(this.fromJsonHelper.parameterExists("isEnableMarketingMails", element)) {
			Boolean isEnableMarketingMails = this.fromJsonHelper.extractBooleanNamed("isEnableMarketingMails", element);
			selfcare.setIsEnableMarketingMails(isEnableMarketingMails);
		}
		
		this.selfCareRepository.save(selfcare);
		return selfcare.getId().toString();
	}
	
}
