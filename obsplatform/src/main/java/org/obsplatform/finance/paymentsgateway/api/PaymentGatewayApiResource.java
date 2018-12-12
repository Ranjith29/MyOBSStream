
package org.obsplatform.finance.paymentsgateway.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.finance.creditdistribution.service.CreditDistributionWritePlatformService;
import org.obsplatform.finance.payments.evo.EvoPaymentGatewayApiResource;
import org.obsplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.obsplatform.finance.payments.service.PaymentWritePlatformService;
import org.obsplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.obsplatform.finance.paymentsgateway.domain.EvoNotify;
import org.obsplatform.finance.paymentsgateway.domain.EvoNotifyRepository;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.service.PaymentGatewayRecurringWritePlatformService;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayReadPlatformService;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.obsplatform.infrastructure.codes.data.CodeData;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.MediaEnumoptionData;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/paymentgateways")
@Component
@Scope("singleton")

/**
 * The class <code>PaymentGatewayApiResource</code> is developed for Third party
 * PaymentGateway systems. Using the below API to Communicate OBS with
 * Adapters/Third-Party servers.
 * 
 * @author ashokreddy
 *
 */
public class PaymentGatewayApiResource {
	
	private final static Logger logger = LoggerFactory.getLogger(PaymentGatewayApiResource.class);
	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSEPARAMETERS = new HashSet<String>(
			Arrays.asList("id","paymentId", "serialNo", "paymentDate", "receiptNo","status","phoneNo","clientName","amountPaid","remarks"));
	
	private final String resourceNameForPermissions = "PAYMENTGATEWAY";

	private final PlatformSecurityContext context;
	private final PaymentGatewayReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;
	private final EvoNotifyRepository evoNotifyRepository;
	private final FromJsonHelper fromJsonHelper;
	private final EvoPaymentGatewayApiResource evoPaymentGatewayApiResource;
	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	private final MCodeReadPlatformService codeReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final PaymentWritePlatformService paymentWritePlatformService;
	private final CreditDistributionWritePlatformService crdwritePlatformService;
	private final OrderWritePlatformService orderWritePlatformService;
	
	private String returnMessage;
	private String success;
	private String errorDesc;
	private String contentData;
	private CommandProcessingResult result;
	private JSONObject jsonData;
	private Long errorCode;

	@Autowired
	public PaymentGatewayApiResource(final PlatformSecurityContext context,
			final PaymentGatewayReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
    		final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService,
    		final EvoNotifyRepository evoNotifyRepository, final FromJsonHelper fromJsonHelper,
    		final EvoPaymentGatewayApiResource evoPaymentGatewayApiResource,
    		final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService,
    		final MCodeReadPlatformService codeReadPlatformService,
    		final PortfolioCommandSourceWritePlatformService writePlatformService,
    		final PaymentWritePlatformService paymentWritePlatformService,
    		final CreditDistributionWritePlatformService crdwritePlatformService,
    		final OrderWritePlatformService orderWritePlatformService) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.context=context;
		this.readPlatformService=readPlatformService;
		this.apiRequestParameterHelper=apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    	this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
    	this.evoNotifyRepository = evoNotifyRepository;
    	this.fromJsonHelper = fromJsonHelper;
    	this.evoPaymentGatewayApiResource = evoPaymentGatewayApiResource; 
    	this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
    	this.codeReadPlatformService = codeReadPlatformService;
    	this.writePlatformService = writePlatformService;
    	this.paymentWritePlatformService = paymentWritePlatformService;
    	this.crdwritePlatformService = crdwritePlatformService;
    	this.orderWritePlatformService = orderWritePlatformService;
	}

	/**
	 * This method <code>onlinePayment</code> is 
	 * used for the Both M-pesa & Tigo-pesa PaymentGateways to Pay the Money.
	 * 
	 * @param requestData
	 * 			Containg input data in the Form of Xml/Soap . 
	 * @return
	 */
	@POST
	@Consumes({ MediaType.WILDCARD })
	@Produces({ MediaType.APPLICATION_XML })
	public String onlinePayment(final String requestData)  {
		
	     try{
			final JSONObject xmlJSONObj = XML.toJSONObject(requestData);
			jsonData=this.returnJsonFromXml(xmlJSONObj);
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createPaymentGateway().withJson(jsonData.toString()).build();
			result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			success = "SUCCESS";
			errorDesc = "";
			errorCode = Long.valueOf(0);	
			contentData = "OBSTRANSACTIONID=" + result.resourceId();	
			return this.returnToServer();	
		}catch(ReceiptNoDuplicateException e){
				success="DUPLICATE_TXN";
				errorDesc="DUPLICATE";
				errorCode=Long.valueOf(1);
				contentData="TXNID ALREADY EXIST";
				return this.returnToServer();
		} catch (JSONException e) {
			    return e.getCause().toString();	 
		} catch (PlatformDataIntegrityException e) {
		        return null;
	    }   
	}

	private String returnToServer() {
		
		try {
			final String obsPaymentType = jsonData.getString("OBSPAYMENTTYPE");
		
			
			if(obsPaymentType.equalsIgnoreCase("MPesa")){
				
					     String receipt=jsonData.getString("receipt");
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
				                .append("<response>")
				                .append("<receipt>"+receipt)
				                .append("</receipt>")
				                .append("<result>"+success)
				                .append("</result>")
				                .append("</response>");
				            returnMessage= builder.toString();
					
			}else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
				
					//String TYPE = jsonData.getString("TYPE");
					String txnId = jsonData.getString("TXNID");
					String customerReferenceNumber = jsonData.getString("CUSTOMERREFERENCEID");	
					String msisdn = jsonData.getString("MSISDN");
					
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\"?>")
				                .append("<!DOCTYPE COMMAND PUBLIC \"-//Ocam//DTD XML Command 1.0//EN\" \"xml/command.dtd\">")
				                .append("<COMMAND>")
				                .append("<TYPE>"+"SYNC_BILLPAY_RESPONSE")
				                .append("</TYPE>")
				                .append("<TXNID>"+txnId)
				                .append("</TXNID>")
				                .append("<REFID>"+customerReferenceNumber)
				                .append("</REFID>")
				                .append("<RESULT>"+success)
				                .append("</RESULT>")
				                .append("<ERRORCODE>"+errorCode)
				                .append("</ERRORCODE>")
				                .append("<ERRORDESC>"+errorDesc)
				                .append("</ERRORDESC>")
				                .append("<MSISDN>"+msisdn)
				                .append("</MSISDN>")
				                .append("<FLAG>"+"Y")
				                .append("</FLAG>")
				                .append("<CONTENT>"+contentData)
				                .append("</CONTENT>")
				                .append("</COMMAND>");
				            
				            returnMessage= builder.toString();			 
					
		}
		return returnMessage;
		} catch (JSONException e) {
			return e.getCause().toString();	 
		}
		
	}

	public JSONObject returnJsonFromXml(JSONObject xmlJSONObj){		
		try {
			JSONObject element=null;
			boolean b=xmlJSONObj.has("COMMAND");
			
			if(b==true){
			    element = xmlJSONObj.getJSONObject("COMMAND");
			    element.put("OBSPAYMENTTYPE", "TigoPesa");
			    element.put("locale", "en");
			}else{
				element = xmlJSONObj.getJSONObject("transaction");
				element.put("OBSPAYMENTTYPE", "MPesa");
				element.put("locale", "en");
			}
			return element;
		} catch (JSONException e) { 
			return null;
		}
		
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,@QueryParam("source") final String source,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,@QueryParam("tabType") final String type) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<PaymentGatewayData> paymentData = readPlatformService.retrievePaymentGatewayData(searchItemDetails,type,source);
		return this.toApiJsonSerializer.serialize(paymentData);

	}
	
	@Path("download")
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response retriveDataForDownload(@Context final UriInfo uriInfo, @QueryParam("source") final String source,
			@QueryParam("status") final String status, @QueryParam("fromDate") final Long fromDate,
			@QueryParam("toDate") final Long toDate) throws IOException {

		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		return this.readPlatformService.retrieveDownloadedData(source, status, fromDate, toDate);
	}
	
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		PaymentGatewayData paymentData = readPlatformService.retrievePaymentGatewayIdData(id);
		List<MediaEnumoptionData> data=readPlatformService.retrieveTemplateData();
		paymentData.setStatusData(data);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSEPARAMETERS);

	}
	
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateData(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaymentGateway(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);

	}

	/**
	 * This method is used for Online Payment 
	 * Systems like Paypal, Dalpay, Korta, authorize.net etc...
	 * 
	 * Storing these payment details in 2 tables.
	 * 1) b_paymentgateway and 
	 * 2) b_payment.
	 * 
	 * Send the Notification Payment Mail to Customer.
	 */

	@PUT
	@Path("onlinepayment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML})
	public String OnlinePaymentMethod(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().onlinePaymentGateway().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return String.valueOf(result.getChanges().get("data"));
	}
	
	@GET
	@Path("authorize")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String authTest(@QueryParam("amount") BigDecimal amount) throws JSONException {
        return this.paymentGatewayWritePlatformService.createFingerPrint(amount);
	}
	
	@POST
	@Path("evoPayment")
	public String evoNotify(@QueryParam("Data") final String data,
			@QueryParam("Len") final Long length) {

		logger.info("*** URL Notify method body starts ***");
		try {

			JsonObject blowfishJson = new JsonObject();
			blowfishJson.addProperty("text", data);
			blowfishJson.addProperty("length", length);

			String blowfishDecryptData = this.evoPaymentGatewayApiResource.blowfishEncrpt(blowfishJson.toString(), "decrypt");				
			
			JsonElement baseElement = this.fromJsonHelper.parse(blowfishDecryptData);
			JsonObject jsonObj = baseElement.getAsJsonObject().getAsJsonObject("map");
			baseElement = this.fromJsonHelper.parse(jsonObj.toString());
			
			String output = returnString("blowfishData", baseElement);
			
			logger.info(output);
			
			String[] splitAmpersand = output.split("&");
			JsonObject outputJson = new JsonObject();
			for (int i = 0; splitAmpersand.length > i; i++) {
				String[] splitEqual = splitAmpersand[i].split("=");
				outputJson.addProperty(splitEqual[0], splitEqual[1]);
			}
			
			baseElement = this.fromJsonHelper.parse(outputJson.toString());		
			String userDataStr = returnString("UserData", baseElement);
			String status = returnString("Status", baseElement);
			String code = returnString("Code", baseElement);
			Long refNo = this.fromJsonHelper.extractLongNamed("refnr", baseElement);
			
			baseElement = this.fromJsonHelper.parse(userDataStr);
			logger.info("userDataString :****::: "+baseElement);
			final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", baseElement);
			final JsonArray depositDistributions = this.fromJsonHelper.extractJsonArrayNamed("depositDistributions", baseElement);
			final JsonArray creditdistributionsArray = this.fromJsonHelper.extractJsonArrayNamed("creditdistributions", baseElement);
			final Long statmentId = this.fromJsonHelper.extractLongNamed("statmentId", baseElement);
			final String avialableAmount = this.fromJsonHelper.extractStringNamed("avialableAmount", baseElement);
			
			
			
			if (status.equalsIgnoreCase("AUTHORIZED") || status.equalsIgnoreCase("OK") && code.equalsIgnoreCase("00000000")) {

				JsonObject postJsonObj = new JsonObject();
				postJsonObj.addProperty("total_amount", returnString("price", baseElement));
				postJsonObj.addProperty("transactionId",refNo);
				postJsonObj.addProperty("source", "EVO");
				postJsonObj.addProperty("otherData", outputJson.toString());
				postJsonObj.addProperty("locale", returnString("locale", baseElement));
				postJsonObj.addProperty("emailId", returnString("email", baseElement));
				postJsonObj.addProperty("currency", "GBP");
				postJsonObj.addProperty("clientId", clientId);
				postJsonObj.add("deposit", depositDistributions);
				postJsonObj.addProperty("paymentType", returnString("paymentType", baseElement));
				postJsonObj.addProperty("statmentId", statmentId);

				final JsonElement element = fromJsonHelper.parse(postJsonObj.toString());
				JsonCommand postJsonObjCommand = new JsonCommand(null,
						postJsonObj.toString(), element, fromJsonHelper, null,
						null, null, null, null, null, null, null, null, null,
						null, null);

				CommandProcessingResult result = this.paymentGatewayWritePlatformService.onlinePaymentGateway(postJsonObjCommand);
				String paymentResult = String.valueOf(result.getChanges().get("data"));
				
				JSONObject obj = new JSONObject(paymentResult);
				String paymentType = returnString("paymentType", baseElement);
				logger.info("*** Result ***:::"+obj.get("result").toString());
			//for invoice level credit distributions
			   if(null != paymentType){
				if((obj.get("result").toString().equalsIgnoreCase("Success")) && (paymentType.equalsIgnoreCase("Invoice"))){
					for(JsonElement jsonelement : creditdistributionsArray){
						jsonelement.getAsJsonObject().addProperty("paymentId", obj.get("obsPaymentId").toString());
					}
					JsonObject jsonObject = new JsonObject();
					jsonObject.add("creditdistributions", creditdistributionsArray);
					jsonObject.addProperty("paymentId", obj.get("obsPaymentId").toString());
					jsonObject.addProperty("locale", returnString("locale", baseElement));
					jsonObject.addProperty("avialableAmount", avialableAmount);
					jsonObject.addProperty("paymentType", returnString("paymentType", baseElement));
					
					final JsonElement elementt = fromJsonHelper.parse(jsonObject.toString());
					JsonCommand jsonObjectCommand = new JsonCommand(null,
							jsonObject.toString(), elementt, fromJsonHelper, null,
							null, null, null, null, null, null, null, null, null,
							null, null);
					this.crdwritePlatformService.createCreditDistribution(jsonObjectCommand);
				 }
				}
			   String screenName = returnString("screenName", baseElement);
			   //for renewal order
			   if((obj.get("result").toString().equalsIgnoreCase("Success")) &&  (screenName.equalsIgnoreCase("renewalOrder"))){
			   
					final Long orderId = this.fromJsonHelper.extractLongNamed("renewalOrderId", baseElement);
					final Long priceId = this.fromJsonHelper.extractLongNamed("priceId", baseElement);
					final String renewalPeriod = this.fromJsonHelper.extractStringNamed("renewalPeriod", baseElement);
					final String description = this.fromJsonHelper.extractStringNamed("description", baseElement);
					
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("priceId", priceId);
					jsonObject.addProperty("renewalPeriod", renewalPeriod);
					if(description!=null) jsonObject.addProperty("renewalPeriod", renewalPeriod);
					
					final JsonElement elementt = fromJsonHelper.parse(jsonObject.toString());
					JsonCommand jsonObjectCommand = new JsonCommand(null,
							jsonObject.toString(), elementt, fromJsonHelper, null,
							null, null, null, null, null, null, null, null, null,
							null, null);
					
					this.orderWritePlatformService.renewalClientOrder(jsonObjectCommand, orderId);
			   }
			   
			 //for change plan
			   if((obj.get("result").toString().equalsIgnoreCase("Success")) &&  (screenName.equalsIgnoreCase("changeOrder"))){
			   
					final Long orderId = this.fromJsonHelper.extractLongNamed("changePlanId", baseElement);
					final JsonArray changeplanArray = this.fromJsonHelper.extractJsonArrayNamed("changePlanJson", baseElement);
					
					JsonObject changePlanJsonObject = changeplanArray.get(0).getAsJsonObject().getAsJsonObject("changePlanJson");
					
					final JsonElement elementt = fromJsonHelper.parse(changePlanJsonObject.toString());
					JsonCommand jsonObjectCommand = new JsonCommand(null,
							changePlanJsonObject.toString(), elementt, fromJsonHelper, null,
							null, null, null, null, null, null, null, null, null,
							null, null);
					
					this.orderWritePlatformService.changePlan(jsonObjectCommand, orderId);
			   }
			   
			 //for newOrder
			   if((obj.get("result").toString().equalsIgnoreCase("Success")) &&  (screenName.equalsIgnoreCase("newOrder"))){
			   
				    final String existActiveOrders = this.fromJsonHelper.extractStringNamed("existActiveOrders", baseElement);
					final Long noOfConnections = this.fromJsonHelper.extractLongNamed("noOfConnections", baseElement);
					final JsonArray newOrderJsonArray = this.fromJsonHelper.extractJsonArrayNamed("newOrderJson", baseElement);
					
					JsonObject newOrderJsonObject = newOrderJsonArray.get(0).getAsJsonObject().getAsJsonObject("newOrderJson");
					
					logger.info("existActiveOrders : ***"+existActiveOrders);
					logger.info("noOfConnections : *******"+noOfConnections);
					logger.info("newOrderJsonObject : *"+newOrderJsonObject);
					
					final JsonElement elementt = fromJsonHelper.parse(newOrderJsonObject.toString());
					JsonCommand jsonObjectCommand = new JsonCommand(null,
							newOrderJsonObject.toString(), elementt, fromJsonHelper, null,
							null, null, null, null, null, null, null, null, null,
							null, null);
					
					for(int i=1;i<= noOfConnections ; i++){
						this.orderWritePlatformService.createOrder(clientId, jsonObjectCommand);
					}
			   }
				
				EvoNotify notify = new EvoNotify(data, length);
				notify.setClientId(clientId);
				notify.setStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
				this.evoNotifyRepository.save(notify);

				logger.info("Record inserted Successfully");
			} else {
				EvoNotify notify = new EvoNotify(data, length);
				notify.setClientId(clientId);
				notify.setStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				this.evoNotifyRepository.save(notify);
			}

			return "Success";
		} catch (Exception exception) {
			logger.error(exception.getMessage(), exception);
			return "Failure";
		}

	}
	
	private String returnString(String parameterName, JsonElement element) {
		return this.fromJsonHelper.extractStringNamed(parameterName, element);
	}
	
	@PUT
	@Path("echeck")
	@Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
	public String echeckProcessing(final String apiRequestBodyAsJson) {
				
		String response = this.paymentGatewayWritePlatformService.echeckProcess(apiRequestBodyAsJson);
		return OnlinePaymentMethod(response);	
	}
	
	@POST
	@Path("authorize")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	@Produces({ MediaType.TEXT_HTML })
	public void authorizedRecurringPayment(final @Context HttpServletRequest request) {
		
		String requestParameters = this.paymentGatewayRecurringWritePlatformService.getRequestParameters(request);
		String subject = "Authorize silentPostTest,  ";		
		sendToUserEmail(subject, requestParameters);		
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
			message.addRecipient(Message.RecipientType.CC,new InternetAddress("sanjeev.2826@gmail.com"));
			message.setSubject(subject);

			StringBuilder messageBuilder = new StringBuilder().append("hi, authorize silentPost Url,  ").append("\n").append(body);

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
			e.printStackTrace();
			logger.error("throwing Exception", e);
		}		
	}

}

