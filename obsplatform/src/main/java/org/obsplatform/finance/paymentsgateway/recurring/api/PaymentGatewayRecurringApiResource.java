package org.obsplatform.finance.paymentsgateway.recurring.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringData;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistoryRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.recurring.exception.RecurringBillingProfileTypeNotFoundException;
import org.obsplatform.finance.paymentsgateway.recurring.service.AuthorizeRecurringBillingProfileReadPlatformService;
import org.obsplatform.finance.paymentsgateway.recurring.service.PaymentGatewayRecurringWritePlatformService;
import org.obsplatform.finance.paymentsgateway.recurring.service.PaypalRecurringBillingProfileProcess;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.obsplatform.infrastructure.codes.data.CodeData;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/recurringpayments")
@Component
@Scope("singleton")

/**
 * The class <code>PaymentGatewayRecurringApiResource</code> is developed for
 * the processing of Third party PaymentGateway's Recurring Payments.
 * Using the below API to Communicate OBS with Adapters/Third-Party servers. 
 * 
 * @author ashokreddy
 *
 */
public class PaymentGatewayRecurringApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "clientId", "orderId", "subscriberId", "gatewayName"));
	
	private final static Logger logger = LoggerFactory.getLogger(PaymentGatewayRecurringApiResource.class);
	private final static String resourceName = "RECURRING";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<RecurringData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;
	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;
	private final PaypalRecurringBillingProfileProcess paypalRecurringBillingProfileProcess;
	private final ApiRequestParameterHelper apiRequestParameterHelper; 
	private final FromJsonHelper fromJsonHelper;
	private final AuthorizeRecurringBillingProfileReadPlatformService authorizeRecurringBillingProfileReadPlatformService;
	
	@Autowired
	public PaymentGatewayRecurringApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<RecurringData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
    		final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
    		final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService,
    		final RecurringBillingHistoryRepository recurringBillingHistoryRepository,
    		final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService,
    		final PaypalRecurringBillingProfileProcess paypalRecurringBillingProfileProcess,
    		final ApiRequestParameterHelper apiRequestParameterHelper,
    		final FromJsonHelper fromJsonHelper,
    		final AuthorizeRecurringBillingProfileReadPlatformService authorizeRecurringBillingProfileReadPlatformService) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.context=context;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    	this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
    	this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
    	this.recurringBillingHistoryRepository = recurringBillingHistoryRepository;
    	this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
    	this.paypalRecurringBillingProfileProcess = paypalRecurringBillingProfileProcess;
    	this.apiRequestParameterHelper = apiRequestParameterHelper;
    	this.fromJsonHelper = fromJsonHelper;
    	this.authorizeRecurringBillingProfileReadPlatformService = authorizeRecurringBillingProfileReadPlatformService;
	}

	@GET
	@Path("{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getRecurringProfileSubscriberId(@PathParam("orderId") final Long orderId, @Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceName);
		final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneByOrderId(orderId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(null == recurringBilling) {
			RecurringData recurringData = new RecurringData(null, "undefined", orderId, null);
			return this.toApiJsonSerializer.serialize(settings, recurringData ,RESPONSE_DATA_PARAMETERS);
		}
		final RecurringData recurringData = RecurringData.getRecurringData(recurringBilling);
		return this.toApiJsonSerializer.serialize(settings, recurringData ,RESPONSE_DATA_PARAMETERS);
	}
	
	/**
	 * We can Use this Method to perform to store the data into Recurring Table.
	 * 
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createRecurringBillingProfile(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createRecurringBillingProfile().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * We can Use this Method to save the OrderId into Recurring Table. So that
	 * we can perform Operations based on OrderId.
	 * 
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateRBOrderId(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRecurringBillingOrderID().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * This method is using for Changing the status of Recurring Billing.
	 * 
	 * @return
	 */
	@POST
	@Path("changestatus")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String paypalChangeRecurringStatus(final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * This method is using for posting data to create payment using paypal
	 * 
	 * @return
	 */
	@PUT
	@Path("updaterecurring")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String paypalUpdateRecurringProfile(final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaypalProfileRecurring().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("delSubscription")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteRecurringSubscriber(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteRecurringBilling().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@POST
	@Path("{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelRecurringBillingProfile(@PathParam("orderId") final Long orderId, final String apiRequestBodyAsJson) {

		final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findRecurringProfile(orderId);
		final String gatewayName = recurringBilling.getGatewayName();
		if (null == gatewayName)
			throw new RecurringBillingProfileTypeNotFoundException(orderId);
		
		if(gatewayName.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET)) {
			
			final String data = processAuthorizeRecurring(apiRequestBodyAsJson);
			recurringBilling.updateStatus();
			this.recurringBillingRepositoryWrapper.save(recurringBilling);
			return data;
			
		} /*else if (gatewayName.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_PAYPAL)) {	
			throw new RecurringBillingProfileTypeNotFoundException(gatewayName);
		}*/ else {
			throw new RecurringBillingProfileTypeNotFoundException(gatewayName);
		}
	}
	
	/**
	 * This method is using for Handling Paypal IPN Requests.
	 * 
	 * i) We have to Verify the Paypal IPN Request by Re-Sending the Received
	 * Parameters to Paypal IPN Server.
	 * 
	 * ii) Paypal IPN Server Checks Whether IPN Server Sending Request
	 * Parameters and Received Parameters(Which are Sending by OBS on (i)).
	 * 
	 * iii) If Both Request Parameters are Match, Then Only Paypal Server Send
	 * "VERIFIED" as Response
	 * 
	 * iv) If Both are mis-match, Then Send "INVALID" as Response.
	 */

	@POST
	@Path("ipnhandler")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	@Produces({ MediaType.TEXT_HTML })
	public void paypalRecurringPayment(final @Context HttpServletRequest request) {
		
		System.out.println("request data :"+request);

		final RecurringBillingHistory recurringBillingHistory = new RecurringBillingHistory();

		try {
			final String verifiyMessage = this.paymentGatewayRecurringWritePlatformService.paypalRecurringVerification(request);
			final String txnType = request.getParameter(RecurringPaymentTransactionTypeConstants.RECURRING_TXNTYPE);
			logger.info("Transaction Type :" + txnType + " , Result:" + verifiyMessage);

			final String requestParameters = this.paymentGatewayRecurringWritePlatformService.getRequestParameters(request);

			recurringBillingHistory.setTransactionData(requestParameters);
			recurringBillingHistory.setTransactionDate(new Date());
			recurringBillingHistory.setSource(RecurringPaymentTransactionTypeConstants.PAYPAL);
			recurringBillingHistory.setTransactionStatus(verifiyMessage);
			recurringBillingHistory.setTransactionCategory(txnType);

			if (request.getParameterMap().containsKey("txn_id")) {
				recurringBillingHistory.setTransactionId(request.getParameter("txn_id"));
			}

			if (RecurringPaymentTransactionTypeConstants.RECURRING_VERIFIED.equals(verifiyMessage)) {

				switch (txnType) {

				case RecurringPaymentTransactionTypeConstants.SUBSCR_SIGNUP:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_PROFILE_CREATED:
					
					this.paymentGatewayRecurringWritePlatformService.recurringSubscriberSignUp(request, recurringBillingHistory);
					break;

				case RecurringPaymentTransactionTypeConstants.SUBSCR_PAYMENT:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT:

					final RecurringBilling paypalRecurringBilling = this.paymentGatewayRecurringWritePlatformService.recurringSubscriberSignUp(request, recurringBillingHistory);

					if (null != paypalRecurringBilling) {

						final String jsonObject = this.paymentGatewayRecurringWritePlatformService.createJsonForOnlineMethod(request);
						final String data = this.paymentGatewayWritePlatformService.onlinePaymentGateway(jsonObject);
				
						JsonElement element = this.fromJsonHelper.parse(data);
						final String result = this.fromJsonHelper.extractStringNamed("result", element);
						final String description = this.fromJsonHelper.extractStringNamed("description", element);

						if (result.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.SUCCESS)) {						
							this.paymentGatewayRecurringWritePlatformService.recurringEventUpdate(request, recurringBillingHistory);
						} else {
							recurringBillingHistory.setClientId(paypalRecurringBilling.getClientId());
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
							recurringBillingHistory.setObsDescription("Payment Failed in OBS, Reason: " + description);
							this.recurringBillingHistoryRepository.save(recurringBillingHistory);
						}
					}
					break;

				case RecurringPaymentTransactionTypeConstants.SUBSCR_EOT:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_EXPIRED:
				case RecurringPaymentTransactionTypeConstants.SUBSCR_CANCELLED:

					final String profileId = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);
					final RecurringBilling billing = this.paypalRecurringBillingProfileProcess.getRecurringBillingObject(profileId);

					if (null == billing || null == billing.getOrderId()) {
						recurringBillingHistory.setClientId(0L);
						recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
						recurringBillingHistory.setObsDescription("OrderId Not Found with this SubscriberId:" + profileId);
					
					} else {
						final String status = this.paypalRecurringBillingProfileProcess.getOrderStatus(billing.getOrderId());
						this.paymentGatewayRecurringWritePlatformService.updateRecurringBillingTable(profileId);

						if (null != status && status.equalsIgnoreCase(StatusTypeEnum.ACTIVE.toString())) {
							final CommandWrapper commandRequest = new CommandWrapperBuilder().terminateOrder(billing.getOrderId()).build();
							final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
							recurringBillingHistory.setClientId(billing.getClientId());

							if (null == result || result.resourceId() <= 0) {
								recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
								recurringBillingHistory.setObsDescription("order Terminate Action Failed...");
							} else {
								recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
								recurringBillingHistory.setObsDescription("Order Termination Completed...");
							}
						}
					}

					this.recurringBillingHistoryRepository.save(recurringBillingHistory);
					break;

				case RecurringPaymentTransactionTypeConstants.SUBSCR_FAILED:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILED:
				case RecurringPaymentTransactionTypeConstants.SUBSCR_MODIFY:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SKIPPED:

					recurringBillingHistory.setClientId(0L);
					recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_UNKNOWN);
					recurringBillingHistory.setObsDescription("UnDeveloped Request types");
					this.recurringBillingHistoryRepository.save(recurringBillingHistory);
					break;

				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUSPENDED:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUSPENDED_DUE_TO_MAX_FAILED_PAYMENT:

					final String profileId1 = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);
					this.paymentGatewayRecurringWritePlatformService.disConnectOrder(profileId1, recurringBillingHistory);
					break;
				
				default:
					break;
				}

			} else {
				recurringBillingHistory.setClientId(0L);
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				recurringBillingHistory.setObsDescription("Paypal Verification Failed...");
				this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			}

		} catch (JSONException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("JSONException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (UnsupportedEncodingException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("UnsupportedEncodingException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (IllegalStateException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("IllegalStateException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (ClientProtocolException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("ClientProtocolException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (IOException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("IOException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		}
	}
	
	private String stackTraceToString(Throwable e) {

		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * We have to use this method to process the All Authorize.Net Request calls.
	 * like Creation of Profile, Cancelling of Profile, Get Profile, updation of profile etc...
	 */
	@POST
	@Path("authorize")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String processAuthorizeRecurring(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().authorizeRecurringBilling().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	
	/**
	 * Authorize.Net SilentPostUrl Uses this Method to Update the Notify Details to OBS.
	 * @param request
	 */
	@POST
	@Path("authorize/silentpost")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	@Produces({ MediaType.TEXT_HTML })
	public void authorizedRecurringPayment(final @Context HttpServletRequest request) {
		
		String requestParameters = this.paymentGatewayRecurringWritePlatformService.getRequestParameters(request);
		String output = this.paymentGatewayRecurringWritePlatformService.notifyAuthorizeRecurringBillingRequest(requestParameters);
		
		if(null != output) {
			
			final String data = this.paymentGatewayWritePlatformService.onlinePaymentGateway(output);
			
			JsonElement element = this.fromJsonHelper.parse(data);
			final String result = this.fromJsonHelper.extractStringNamed("result", element);
			final String description = this.fromJsonHelper.extractStringNamed("description", element);

			JsonElement elementOutput = this.fromJsonHelper.parse(output);
			
			if(this.fromJsonHelper.parameterExists("recurringHistoryId", elementOutput)) {
				final Long id = this.fromJsonHelper.extractLongNamed("recurringHistoryId", elementOutput);
				final RecurringBillingHistory recurringBillingHistory = this.recurringBillingHistoryRepository.findOne(id);
				recurringBillingHistory.setObsStatus(result);
				recurringBillingHistory.setObsDescription("Payment Output in OBS: " + description);
				this.recurringBillingHistoryRepository.save(recurringBillingHistory);		
			}	
		}
		String subject = "Authorize silentPostTest ";		
		sendToUserEmail(subject, requestParameters);
		/*final CommandWrapper commandRequest = new CommandWrapperBuilder().notifyAuthorizeRequest().withJson(requestParameters).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);*/
		//return String.valueOf(result.getChanges().get("data"));
	}

	public void sendToUserEmail(String subject, String body) {
		
		try {
			
			int portNumber = 587;
			final String mailId = "ashokreddy556@gmail.com";
			final String password = "9989720715";
			String hostName = "smtp.gmail.com";
			String email = "ashokcse556@gmail.com";
			boolean starttls = true;
			
			String setContentString = "text/html;charset=utf-8";
			
			Properties properties = System.getProperties();  
			
		     properties.setProperty("mail.smtp.host", hostName);   
		     properties.put("mail.smtp.ssl.trust",hostName);
		     properties.put("mail.smtp.auth", "true");  
		     properties.put("mail.smtp.starttls.enable", starttls);//put as false
		     properties.put("mail.smtp.starttls.required", starttls);//put as false
		     properties.put("mail.smtp.port", portNumber);
		
		
			Session session = Session.getDefaultInstance(properties,   
		             new javax.mail.Authenticator() {   
		         protected PasswordAuthentication getPasswordAuthentication() {   
		             return new PasswordAuthentication(mailId,password);    }   }); 
			
			//2) compose message      
			MimeMessage message = new MimeMessage(session);
			//message.setFrom(new InternetAddress(emailDetail.getMessageFrom()));
			
			message.setFrom(new InternetAddress(mailId));
			message.addRecipient(Message.RecipientType.TO,new InternetAddress(email));
			message.setSubject(subject);

			StringBuilder messageBuilder = new StringBuilder().append("hi, authorize silentPost Url, ").append("\n").append(body);

			// 3) create MimeBodyPart object and set your message text
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(messageBuilder.toString(),setContentString);
			
			
			// 5) create Multipart object and add MimeBodyPart objects to this object
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			
			// 6) set the multiplart object to the message object
			message.setContent(multipart);

			// 7) send message
			Transport.send(message);

		} catch (Exception e) {
			logger.error("throwing Exception, Reason:" + stackTraceToString(e));
		}		
	}
	@GET
	@Path("authorize/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDepartmentData(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceName);
	    final List<RecurringData> recurringdatas = this.authorizeRecurringBillingProfileReadPlatformService.retrieveRecurringData(clientId);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, recurringdatas,RESPONSE_DATA_PARAMETERS);
	}
}
