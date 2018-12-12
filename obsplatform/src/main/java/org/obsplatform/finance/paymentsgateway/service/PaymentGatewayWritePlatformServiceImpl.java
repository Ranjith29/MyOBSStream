package org.obsplatform.finance.paymentsgateway.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLContext;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.ResponseField;
import net.authorize.TransactionType;
import net.authorize.aim.Result;
import net.authorize.aim.Transaction;
import net.authorize.api.contract.v1.CreateTransactionRequest;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.CreditCardType;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.NameAndAddressType;
import net.authorize.api.contract.v1.PaymentType;
import net.authorize.api.contract.v1.TransactionRequestType;
import net.authorize.api.contract.v1.TransactionResponse;
import net.authorize.api.contract.v1.TransactionResponse.Errors.Error;
import net.authorize.api.contract.v1.TransactionTypeEnum;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.data.EmailReceipt;
import net.authorize.data.echeck.BankAccountType;
import net.authorize.data.echeck.ECheck;
import net.authorize.data.echeck.ECheckType;
import net.authorize.sim.Fingerprint;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.obsplatform.billing.selfcare.api.SelfCareApiResource;
import org.obsplatform.commands.domain.CommandSource;
import org.obsplatform.commands.domain.CommandSourceRepository;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.crm.ticketmaster.api.TicketMasterApiResource;
import org.obsplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.obsplatform.finance.payments.service.PaymentReadPlatformService;
import org.obsplatform.finance.payments.service.PaymentWritePlatformService;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistoryRepository;
import org.obsplatform.finance.paymentsgateway.serialization.PaymentGatewayCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.configuration.data.ConfigurationPropertyData;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.address.domain.AddressRepository;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.employee.data.EmployeeData;
import org.obsplatform.organisation.employee.service.EmployeeReadPlatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.message.domain.BillingMessage;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.obsplatform.organisation.message.exception.EmailNotFoundException;
import org.obsplatform.portfolio.client.api.ClientCardDetailsApiResource;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientCardDetails;
import org.obsplatform.portfolio.client.domain.ClientCardDetailsRepository;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.client.service.ClientCardDetailsReadPlatformService;
import org.obsplatform.portfolio.client.service.ClientCardDetailsWritePlatformService;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.obsplatform.workflow.eventaction.data.OrderNotificationData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventaction.service.EventActionReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.worldpay.gateway.clearwater.client.core.dto.CountryCode;
import com.worldpay.gateway.clearwater.client.core.dto.CurrencyCode;
import com.worldpay.gateway.clearwater.client.core.dto.common.Address;
import com.worldpay.gateway.clearwater.client.core.dto.request.OrderRequest;
import com.worldpay.gateway.clearwater.client.core.dto.response.OrderResponse;
import com.worldpay.gateway.clearwater.client.core.exception.WorldpayException;
import com.worldpay.sdk.WorldpayRestClient;


@SuppressWarnings("deprecation")
@Service
public class PaymentGatewayWritePlatformServiceImpl implements PaymentGatewayWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(PaymentGatewayWritePlatformServiceImpl.class);
	private final static DateFormat readFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
	private final static String formatOfDate = "dd MMMM yyyy";
	private final static SimpleDateFormat daformat = new SimpleDateFormat(formatOfDate);
	private String currency;
	private static String baseurl = "https://api.worldpay.com/v1";
	
	private final PlatformSecurityContext context;
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final PaymentGatewayCommandFromApiJsonDeserializer paymentGatewayCommandFromApiJsonDeserializer;
	private final FromJsonHelper fromApiJsonHelper;
	private final PaymentGatewayReadPlatformService readPlatformService;
	private final PaymentWritePlatformService paymentWritePlatformService;
	private final PaymentReadPlatformService paymodeReadPlatformService;
	private final CommandSourceRepository commandSourceRepository;
	private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private final BillingMessageRepository messageDataRepository;
	private final ClientRepository clientRepository;
	private final EventActionRepository eventActionRepository;
	private final ConfigurationRepository configurationRepository;
	private final ConfigurationReadPlatformService configReadPlatformService;
	private final AppUserRepository appUserRepository;
	private final MCodeReadPlatformService codeReadPlatformService;
	private final TicketMasterApiResource ticketMasterApiResource;
	private BillingMessageTemplate messageDetails;
	private final EventActionReadPlatformService eventActionReadPlatformService;
	private final FromJsonHelper fromJsonHelper;

	private final DepartmentReadPlatformService departmentReadPlatformService;
	private final EmployeeReadPlatformService employeeReadPlatformService;
	
	private final ClientReadPlatformService  clientReadPlatformService;
	private final ClientCardDetailsWritePlatformService clientCardDetailsWritePlatformService;
	private final ClientCardDetailsApiResource clientCardDetailsApiResource;
	private final SelfCareApiResource selfCareApiResource;
	private final AddressRepository  addressRepository;
	private final AddressReadPlatformService addressReadPlatformService;
	private final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService;
	private final ClientCardDetailsRepository clientCardDetailsRepository;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;
	
	 
	  	   
	@Autowired
	public PaymentGatewayWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final PaymentGatewayRepository paymentGatewayRepository,
			final FromJsonHelper fromApiJsonHelper,
			final PaymentGatewayCommandFromApiJsonDeserializer paymentGatewayCommandFromApiJsonDeserializer,
			final PaymentGatewayReadPlatformService readPlatformService,
			final PaymentWritePlatformService paymentWritePlatformService,
			final PaymentReadPlatformService paymodeReadPlatformService,
			final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService,
			final CommandSourceRepository commandSourceRepository,
			final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final BillingMessageRepository messageDataRepository,
			final ClientRepository clientRepository,
			final EventActionRepository eventActionRepository,
			final ConfigurationRepository configurationRepository,
			final ConfigurationReadPlatformService configReadPlatformService,
			final AppUserRepository appUserRepository,
			final MCodeReadPlatformService codeReadPlatformService,
			final TicketMasterApiResource ticketMasterApiResource,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final FromJsonHelper fromJsonHelper,
			final DepartmentReadPlatformService departmentReadPlatformService,
			final EmployeeReadPlatformService employeeReadPlatformService,
			final ClientReadPlatformService  clientReadPlatformService,
			final ClientCardDetailsWritePlatformService clientCardDetailsWritePlatformService,
			final ClientCardDetailsApiResource clientCardDetailsApiResource,
			final SelfCareApiResource selfCareApiResource,
			final AddressRepository  addressRepository,
			final AddressReadPlatformService addressReadPlatformService,
			final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService,
			final ClientCardDetailsRepository clientCardDetailsRepository,
			final RecurringBillingHistoryRepository recurringBillingHistoryRepository) {
		this.context = context;
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.paymentGatewayCommandFromApiJsonDeserializer = paymentGatewayCommandFromApiJsonDeserializer;
		this.readPlatformService = readPlatformService;
		this.paymentWritePlatformService = paymentWritePlatformService;
		this.paymodeReadPlatformService = paymodeReadPlatformService;
		this.commandSourceRepository = commandSourceRepository;
		this.paymentGatewayConfigurationRepositoryWrapper = paymentGatewayConfigurationRepositoryWrapper;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messageDataRepository = messageDataRepository;
		this.clientRepository = clientRepository;
		this.eventActionRepository = eventActionRepository;
		this.configurationRepository = configurationRepository;
		this.configReadPlatformService = configReadPlatformService;
		this.appUserRepository = appUserRepository;
		this.codeReadPlatformService = codeReadPlatformService;
		this.ticketMasterApiResource = ticketMasterApiResource;
		this.eventActionReadPlatformService = eventActionReadPlatformService;
		this.fromJsonHelper = fromJsonHelper;
		this.departmentReadPlatformService=departmentReadPlatformService;
		this.employeeReadPlatformService=employeeReadPlatformService;
		this.clientReadPlatformService=clientReadPlatformService;
		this.clientCardDetailsWritePlatformService=clientCardDetailsWritePlatformService;
		this.clientCardDetailsApiResource=clientCardDetailsApiResource;
		this.selfCareApiResource=selfCareApiResource;
		this.addressRepository=addressRepository;
		this.addressReadPlatformService=addressReadPlatformService;
		this.clientCardDetailsReadPlatformService=clientCardDetailsReadPlatformService;
		this.clientCardDetailsRepository=clientCardDetailsRepository;
		this.recurringBillingHistoryRepository=recurringBillingHistoryRepository;
		
	}

	private String returnString(final String parameterName, final JsonElement element) {
		return fromApiJsonHelper.extractStringNamed(parameterName, element);
	}

	private BigDecimal returnBigDecimal(final String parameterName, final JsonElement element) {
		return fromApiJsonHelper.extractBigDecimalWithLocaleNamed(parameterName, element);
	}

	private Long returnLongValue(final String parameterName, final JsonElement element) {
		return fromApiJsonHelper.extractLongNamed(parameterName, element);
	}
	    
	private Long mPesaTransaction(final JsonElement element) {

		try {
			final String serialNumberId = returnString("reference", element);
			final String paymentDate = returnString("timestamp", element);
			final BigDecimal amountPaid = returnBigDecimal("amount", element);
			final String phoneNo = returnString("msisdn", element);
			final String receiptNo = returnString("receipt", element);
			final String details = returnString("name", element);
			final String source = ConfigurationConstants.PAYMENTGATEWAY_MPESA;

			Date date = readFormat.parse(paymentDate);

			final PaymentGateway paymentGateway = PaymentGateway.getPaymentGateway(serialNumberId, phoneNo, date, amountPaid, receiptNo, source, details);

			final Long clientId = this.readPlatformService.retrieveClientIdForProvisioning(serialNumberId);

			if (clientId != null && clientId > 0) {

				Long paymodeId = this.paymodeReadPlatformService.getOnlinePaymode("Online Payment");

				if (paymodeId == null) {
					paymodeId = Long.valueOf(83);
				}

				final StringBuilder builder = new StringBuilder("customerName: ").append(details)
						.append(" ,PhoneNo: ").append(phoneNo).append(" ,Biller account Name : ").append(source);
				
				final String paymentdate = daformat.format(date);
				JsonObject object = new JsonObject();
				object.addProperty("dateFormat", formatOfDate);
				object.addProperty("locale", "en");
				object.addProperty("paymentDate", paymentdate);
				object.addProperty("amountPaid", amountPaid);
				object.addProperty("isChequeSelected", "no");
				object.addProperty("receiptNo", receiptNo);
				object.addProperty("remarks", builder.toString());
				object.addProperty("paymentCode", paymodeId);
				final String entityName = "PAYMENT";
				
				final JsonElement paymentElement = fromApiJsonHelper.parse(object.toString());
				final JsonCommand comm = new JsonCommand(null, object.toString(), paymentElement, fromApiJsonHelper, 
						entityName, clientId, null, null, null, null, null, null, null, null, null, null);

				final CommandProcessingResult result = this.paymentWritePlatformService.createPayment(comm);
				if (result.resourceId() != null) {
					paymentGateway.setObsId(result.resourceId());
					paymentGateway.setPaymentId(result.resourceId().toString());
					paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
					paymentGateway.setAuto(false);
					this.paymentGatewayRepository.save(paymentGateway);
					return result.resourceId();
				} else {
					paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
					paymentGateway.setRemarks("Payment is Not Processed .");
					this.paymentGatewayRepository.save(paymentGateway);
					return Long.valueOf(-1);
				}
				
			} else {
				paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
				paymentGateway.setRemarks("Hardware with this " + serialNumberId + " not Found.");
				this.paymentGatewayRepository.save(paymentGateway);
				return Long.valueOf(-1);
			}
		
		} catch (ParseException e) {
			return Long.valueOf(-1);
		}

	}

	private Long tigoPesaTransaction(final JsonElement element) {

		final String serialNumberId = returnString("CUSTOMERREFERENCEID", element);
		final String txnId = returnString("TXNID", element);
		final BigDecimal amountPaid = returnBigDecimal("AMOUNT", element);
		final String phoneNo = returnString("MSISDN", element);
		final String type = returnString("TYPE", element);
		final String tStatus = returnString("STATUS", element);
		final String details = returnString("COMPANYNAME", element);
		final Date date = DateUtils.getDateOfTenant();
		final String source = ConfigurationConstants.PAYMENTGATEWAY_TIGO;

		PaymentGateway paymentGateway = new PaymentGateway(serialNumberId, txnId, amountPaid, phoneNo, 
				type, tStatus, details, date, source);

		final Long clientId = this.readPlatformService.retrieveClientIdForProvisioning(serialNumberId);

		if (clientId != null && clientId > 0) {
			Long paymodeId = this.paymodeReadPlatformService.getOnlinePaymode("M-pesa");
			if (paymodeId == null) {
				paymodeId = Long.valueOf(83);
			}
			
			final StringBuilder builder = new StringBuilder("companyName: ").append(details)
					.append(" ,PhoneNo: ").append(phoneNo).append(" ,Biller account Name : ").append(source)
					.append(" ,Type:").append(type).append(" ,Status:").append(tStatus);

			final String paymentdate = daformat.format(date);
			JsonObject object = new JsonObject();
			object.addProperty("dateFormat", formatOfDate);
			object.addProperty("locale", "en");
			object.addProperty("paymentDate", paymentdate);
			object.addProperty("amountPaid", amountPaid);
			object.addProperty("isChequeSelected", "no");
			object.addProperty("receiptNo", txnId);
			object.addProperty("remarks", builder.toString());
			object.addProperty("paymentCode", paymodeId);
			final String entityName = "PAYMENT";
			
			final JsonElement paymentElement = fromApiJsonHelper.parse(object.toString());
			final JsonCommand comm = new JsonCommand(null, object.toString(), paymentElement, fromApiJsonHelper, 
					entityName, clientId, null, null, null, null, null, null, null, null, null, null);

			final CommandProcessingResult result = this.paymentWritePlatformService.createPayment(comm);
			
			if (result.resourceId() != null) {
				paymentGateway.setObsId(result.resourceId());
				paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
				paymentGateway.setAuto(false);
				this.paymentGatewayRepository.save(paymentGateway);
			} else {
				paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
				paymentGateway.setRemarks("Payment is Not Processed .");
				this.paymentGatewayRepository.save(paymentGateway);
			}
			return result.resourceId();
		} else {
			paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			paymentGateway.setRemarks("Hardware with this " + serialNumberId + " not Found.");
			this.paymentGatewayRepository.save(paymentGateway);
			return new Long(-1);
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult createPaymentGateway(final JsonCommand command) {
		
		
		final JsonElement element = fromApiJsonHelper.parse(command.json());
		
		try {
			context.authenticatedUser();
			this.paymentGatewayCommandFromApiJsonDeserializer.validateForCreate(command.json());

			final String obsPaymentType = returnString("OBSPAYMENTTYPE", element);
			Long resourceId = null;
			if (obsPaymentType.equalsIgnoreCase("MPesa")) {
				resourceId = this.mPesaTransaction(element);
			} else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
				resourceId = this.tigoPesaTransaction(element);
			} 
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(resourceId).build();
		
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(element, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(JsonElement element,
			DataIntegrityViolationException dve) {

		final Throwable realCause = dve.getMostSpecificCause();

		if (realCause.getMessage().contains("reference")) {
			
			final String name = returnString("reference", element);
			throw new PlatformDataIntegrityException("error.msg.code.reference", "A reference with this value '" + name + "' does not exists");

		} else if (realCause.getMessage().contains("receipt_no")) {

			final String obsPaymentType = returnString("OBSPAYMENTTYPE", element);

			if (obsPaymentType.equalsIgnoreCase("MPesa")) {
				throw new ReceiptNoDuplicateException(returnString("receipt", element));
			} else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
				throw new ReceiptNoDuplicateException(returnString("TXNID", element));
			}
		}

		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause);

	}
		
	@Override
	public CommandProcessingResult updatePaymentGateway(final JsonCommand command) {

		this.context.authenticatedUser();
		this.paymentGatewayCommandFromApiJsonDeserializer.validateForUpdate(command.json());
		final PaymentGateway gateway = this.paymentGatewayRepository.findOne(command.entityId());
		final Map<String, Object> changes = gateway.fromJson(command);
		this.paymentGatewayRepository.save(gateway);

		return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				.withEntityId(gateway.getId()).with(changes).build();
	}
		
	private String editGlobalScript(final String MerchantTxnRef, final String merchantId,
			final String userName, final String password) {

		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soap12:Body>"
				+ "<getTransactions xmlns=\"https://www.eazypaynigeria.com/globalpay_demo/\">"
				+ "<merch_txnref>" + MerchantTxnRef + "</merch_txnref>"
				+ "<channel></channel>" + "<merchantID>" + merchantId
				+ "</merchantID>" + "<start_date></start_date>"
				+ "<end_date></end_date>" + "<uid>" + userName + "</uid>"
				+ "<pwd>" + password + "</pwd>"
				+ "<payment_status></payment_status>" + "</getTransactions>"
				+ "</soap12:Body>" + "</soap12:Envelope>";
	}
	
	private String processPostUrl(String data) throws IOException {
		
		HttpURLConnection soapConnection = null;
		BufferedReader bufferReader = null;
		OutputStream reqStream = null;
		
		try {
			URL oURL = new URL(ConfigurationConstants.GLOBALPAY_URL);
			soapConnection = (HttpURLConnection) oURL.openConnection();

			// Send SOAP Message to SOAP Server
			soapConnection.setRequestMethod("POST");
			soapConnection.setRequestProperty("Host", ConfigurationConstants.GLOBALPAY_HOST);
			soapConnection.setRequestProperty("Content-Length", String.valueOf(data.length()));
			soapConnection.setRequestProperty("Content-Type", ConfigurationConstants.GLOBALPAY_CHARSET);
			soapConnection.setRequestProperty("SoapAction", "");
			soapConnection.setDoOutput(true);

			reqStream = soapConnection.getOutputStream();
			reqStream.write(data.toString().getBytes());
			StringBuilder responseSB = new StringBuilder();
			bufferReader = new BufferedReader(new InputStreamReader(soapConnection.getInputStream()));
			String line;
			while ((line = bufferReader.readLine()) != null) {
				responseSB.append(line);
			}

			responseSB.append(line);
			String responseSB1 = responseSB.toString().replaceAll("&lt;", "<");
			responseSB1 = responseSB1.replaceAll("&gt;", ">");
            return responseSB1;
		
		} finally {
  
			if(null != bufferReader) {
				try {
					bufferReader.close();
				} catch (IOException ioException) {
					logger.error("throwing IOException at the Buffer Reader closing, cause:", ioException);
				}
			}
			
			if(null != reqStream) {
				try {
					reqStream.close();
				} catch (IOException ioException) {
					logger.error("throwing IOException at the readerStream closing, cause:", ioException);
				}
			}			
		}
	}
		
	// For Globalpay
	@Override
	public String globalPayProcessing(final String MerchantTxnRef, final String jsonData)
			throws JSONException, IOException {

		final String pgConfigValue = this.paymentGatewayConfigurationRepositoryWrapper
				.getValue(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY);

		final JsonElement pgConfigElement = this.fromApiJsonHelper.parse(pgConfigValue);
		
		final JSONObject pgConfigJsonObj = new JSONObject();
		
		final String merchantId = returnString("merchantId", pgConfigElement);
		final String userName = returnString("userName", pgConfigElement);
		final String password = returnString("password", pgConfigElement);

		final String data = editGlobalScript(MerchantTxnRef, merchantId, userName, password);
		
        final String outputData = processPostUrl(data);
		
		final JSONObject transactionResultset = XML.toJSONObject(outputData)
				.getJSONObject("soap:Envelope").getJSONObject("soap:Body")
				.getJSONObject("getTransactionsResponse")
				.getJSONObject("getTransactionsResult");

		final String resultsetString = (String) transactionResultset.get("resultset").toString();

		if (resultsetString.equalsIgnoreCase("")) {

			String[] clientIdString = MerchantTxnRef.split("-");
			pgConfigJsonObj.put("status", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			pgConfigJsonObj.put("error", "failure : Invalid Merchant TransactionId");
			pgConfigJsonObj.put("clientId", clientIdString[0]);
			pgConfigJsonObj.put("total_amount", 0);
			pgConfigJsonObj.put("transactionId", MerchantTxnRef);
			pgConfigJsonObj.put("currency", "");
			pgConfigJsonObj.put("source", ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY);
			pgConfigJsonObj.put("otherData", jsonData);

			return pgConfigJsonObj.toString();

		} else {

			JSONObject resultset = transactionResultset.getJSONObject("resultset").getJSONObject("record");

			String paymentDesc = resultset.getString("payment_status_description");
			/* System.out.println("paymentDesc From Globalpay: "+ paymentDesc); */
			Double amount = resultset.getDouble("amount");
			String paymentDate = resultset.getString("payment_date");
			Long txnref = resultset.getLong("txnref");
			String channel = resultset.getString("channel");
			String paymentStatus = resultset.getString("payment_status");

			JSONArray fieldArray = resultset.getJSONObject("field_values").getJSONObject("field_values").getJSONArray("field");
			String currency = fieldArray.getJSONObject(2).getString("currency");
			String emailAddress = fieldArray.getJSONObject(3).getString("email_address");

			String globalpayMerchanttxnref = null;

			if (fieldArray.getJSONObject(5).has("merch_txnref")) {
				globalpayMerchanttxnref = fieldArray.getJSONObject(5).getString("merch_txnref");
			} else {
				globalpayMerchanttxnref = fieldArray.getJSONObject(5).getString("merchant_txnref");
			}
			String[] clientIdString = globalpayMerchanttxnref.split("-");

			if (paymentStatus.equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_SUCCESS)) {
				pgConfigJsonObj.put("status", ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
				pgConfigJsonObj.put("error", paymentDesc);
			} else if (paymentStatus.equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_PENDING)) {
				pgConfigJsonObj.put("status", ConfigurationConstants.PAYMENTGATEWAY_PENDING);
				pgConfigJsonObj.put("error", paymentDesc);
			} else {
				pgConfigJsonObj.put("status", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
				pgConfigJsonObj.put("error", paymentDesc);
			}

			JSONObject otherDataObject = new JSONObject();
			otherDataObject.put("currency", currency);
			otherDataObject.put("paymentStatus", paymentStatus);
			otherDataObject.put("channel", channel);
			otherDataObject.put("paymentDate", paymentDate);
			otherDataObject.put("paymentDesc", paymentDesc);
			otherDataObject.put("globalpayMerchanttxnref", globalpayMerchanttxnref);

			pgConfigJsonObj.put("clientId", clientIdString[0]);
			pgConfigJsonObj.put("emailId", emailAddress);
			pgConfigJsonObj.put("transactionId", txnref);
			pgConfigJsonObj.put("total_amount", String.valueOf(amount));
			pgConfigJsonObj.put("source", ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY);
			pgConfigJsonObj.put("otherData", otherDataObject);
			pgConfigJsonObj.put("device", "");
			pgConfigJsonObj.put("currency", currency);

			return pgConfigJsonObj.toString();
		}

	}
	
	// For Neteller Payment Gateway
	private String netellerProcessing(String commandJson) throws JSONException,
			ClientProtocolException, IOException, ParseException {

		final String pgConfigValue = this.paymentGatewayConfigurationRepositoryWrapper
				.getValue(ConfigurationConstants.NETELLER_PAYMENTGATEWAY);
		
		final JsonElement pgConfigElement = this.fromApiJsonHelper.parse(pgConfigValue);
		
		final String url = returnString("url", pgConfigElement);
		final String netellerClientId = returnString("clientId", pgConfigElement);
		final String secretCode = returnString("secretCode", pgConfigElement);

		final JsonElement element = this.fromApiJsonHelper.parse(commandJson);

		String transactionId = returnString("transactionId", element);
		String value = returnString("value", element);
		String currency = returnString("currency", element);
		BigDecimal amount = returnBigDecimal("total_amount", element);
		String verificationCode = returnString("verificationCode", element);
		Long clientId = returnLongValue("clientId", element);

		String credentials = netellerClientId.trim() + ":" + secretCode.trim();
		byte[] encoded = Base64.encodeBase64(credentials.getBytes());
		String encodePassword = new String(encoded);
		String tokenGenerateURL = url + ConfigurationConstants.NETELLER_ACCESS_TOKEN;

		String tokenOutput = processPostNetellerRequests(tokenGenerateURL,
				encodePassword, "", ConfigurationConstants.NETELLER_BASIC);

		String validatingOutput = validatingNetellerOutput(tokenOutput);

		if (validatingOutput.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS)) {

			JSONObject obj = new JSONObject(tokenOutput);

			String token = obj.getString("accessToken");
			String tokenType = obj.getString("tokenType");

			JSONObject transactionObject = new JSONObject();
			JSONObject paymentMethodObject = new JSONObject();

			BigDecimal totalAmount = amount.multiply(new BigDecimal(100));

			JSONObject paymentObject = new JSONObject();

			paymentMethodObject.put("type", ConfigurationConstants.NETELLER_PAYMENTGATEWAY);
			paymentMethodObject.put("value", value); // test member emailId

			transactionObject.put("merchantRefId", transactionId);
			transactionObject.put("amount", totalAmount); // amount to payment
			transactionObject.put("currency", currency); // test member currency

			paymentObject.put("paymentMethod", paymentMethodObject);
			paymentObject.put("transaction", transactionObject);
			paymentObject.put("verificationCode", verificationCode); // test member secureId

			String netellerPaymentURL = url + ConfigurationConstants.NETELLER_PAYMENT;

			String paymentOutput = processPostNetellerRequests(netellerPaymentURL, token, paymentObject.toString(), tokenType);

			String validatingPaymentOutput = validatingNetellerOutput(paymentOutput);

			if (validatingPaymentOutput.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS)) {
				JSONObject outputProcessing = new JSONObject(paymentOutput).getJSONObject("transaction");

				String merchantRefId = outputProcessing.getString("merchantRefId").trim();
				String status = outputProcessing.getString("status");

				if (merchantRefId.equalsIgnoreCase(transactionId) && status.equalsIgnoreCase("accepted")) {
					String netellerId = outputProcessing.getString("id");
					String createDate = outputProcessing.getString("createDate");

					JSONObject otherDataObject = new JSONObject();
					otherDataObject.put("currency", currency);
					otherDataObject.put("paymentStatus", status);
					otherDataObject.put("paymentDate", createDate);
					otherDataObject.put("Neteller_Id", netellerId);
					otherDataObject.put("MerchantRefId", transactionId);

					JSONObject returnObject = new JSONObject();
					returnObject.put("clientId", clientId);
					returnObject.put("transactionId", netellerId);
					returnObject.put("total_amount", String.valueOf(amount));
					returnObject.put("source", ConfigurationConstants.NETELLER_PAYMENTGATEWAY);
					returnObject.put("otherData", otherDataObject);
					returnObject.put("device", "");
					returnObject.put("currency", currency);
					returnObject.put("status", ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);

					return returnObject.toString();

				} else {
					if (!merchantRefId.equalsIgnoreCase(transactionId)) {
						return "failure : TransactionId=" + transactionId + "and Neteller Id=" + merchantRefId
								+ " Should be equal and Transaction Status=" + status;
					} else {
						return "failure : Transaction Status=" + status;
					}
				}
			} else {
				return validatingPaymentOutput;
			}

		} else {
			return validatingOutput;
		}
	}
	
	@Override
	public String onlinePaymentGateway(final String commandJson) {

		String commandJsonValue = null;
		
		this.paymentGatewayCommandFromApiJsonDeserializer.validateForOnlinePayment(commandJson);		
		final JsonElement element = this.fromApiJsonHelper.parse(commandJson);

		try {

			final String source = returnString("source", element);
			final String transactionId = returnString("transactionId", element);

			if (source.equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY)) {

				commandJsonValue = globalPayProcessing(transactionId, commandJson);

			} else if (source.equalsIgnoreCase(ConfigurationConstants.NETELLER_PAYMENTGATEWAY)) {

				commandJsonValue = netellerProcessing(commandJson);

				if (commandJsonValue.contains("failure :")) {

					JsonObject json = element.getAsJsonObject();
					json.addProperty("status", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
					json.addProperty("error", commandJsonValue);

					commandJsonValue = json.toString();
				}

			} else {
				commandJsonValue = commandJson;
			}

			return processOnlinePayment(commandJson);

		} /*catch (DataIntegrityViolationException dve) {

			final Throwable realCause = dve.getMostSpecificCause();

			if (realCause.getMessage().contains("receipt_no")) {
				throw new ReceiptNoDuplicateException(returnString("transactionId", element));
			} else {
				logger.error("throwing DataIntegrityViolationException in OnlinePayment(). ", dve);
				return null;
			}
			
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			paymentGateway.setStatus("Failure");
			paymentGateway.setRemarks(ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION + txnId + " in Payments");
			
			withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_ALREADY_EXIST);
			withChanges.addProperty("description", ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION);
			withChanges.addProperty("amount", amount);
			withChanges.addProperty("obsPaymentId", "");
			withChanges.addProperty("transactionId", txnId);
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();

		} */catch (JSONException exception) {
			logger.error("throwing JSONException in OnlinePayment(). ", exception);
			return null;

		} catch (IOException exception) {
			logger.error("throwing IOException in OnlinePayment(). ", exception);
			return null;

		} catch (ParseException exception) {
			logger.error("throwing ParseException in OnlinePayment(). ", exception);
			return null;
		}

	}

	@Override
	public CommandProcessingResult onlinePaymentGateway(final JsonCommand command) {

		context.authenticatedUser();
		String returnOutput = onlinePaymentGateway(command.json());
		final JsonElement element = this.fromApiJsonHelper.parse(command.json());
		String locale = this.fromApiJsonHelper.extractStringNamed("locale", element);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		try {
			createTicket(returnOutput,locale,clientId);
		} catch (JSONException e) {
			logger.error("throwing Exception while creating a ticket. ", e.getMessage());
		}
		
        
		if(null == returnOutput) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		Map<String, Object> withChanges = new HashMap<String, Object>();
		withChanges.put("data", returnOutput);

		return new CommandProcessingResultBuilder().with(withChanges).build();
	}
	/** 
	 * This method is for Ticket Generation when OnlinePayment done
     */
	private void createTicket(String returnOutput,String locale,Long clientId) throws JSONException{
		final JsonElement element = this.fromApiJsonHelper.parse(returnOutput);
		JsonObject obj = element.getAsJsonObject();
		obj.addProperty("locale", locale);
		
		String paymentResult = this.fromApiJsonHelper.extractStringNamed("result", obj);
		Long paymentId = this.fromApiJsonHelper.extractLongNamed("obsPaymentId", obj);
		BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", obj);
		
		if(paymentResult.equalsIgnoreCase("Success")){
			ConfigurationPropertyData configurationPropertyData = this.configReadPlatformService.retrieveGlobalConfigurationByName(ConfigurationConstants.CONFIG_APPUSER);
			if(configurationPropertyData.isEnabled()){
				String deptName = configurationPropertyData.getValue();
				String departname = deptName.substring(1, deptName.length()-1);
				final DepartmentData departmentDatas = this.departmentReadPlatformService.retrieveDepartmentId(departname);
				Long departmentId =departmentDatas.getId();
				final List<EmployeeData> employeedata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(departmentId);
				Long assigned = null;
				for(EmployeeData employeedatas :employeedata){
					if(employeedatas.isIsprimary()){
						assigned = employeedatas.getUserId();
						//AppUser appuser = (AppUser)this.appUserRepository.findOne(assigned);
					}
				}
				//AppUser appuser = (AppUser)this.appUserRepository.findByUsername(assigned);
				
				
				Collection<MCodeData> problemCode = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROBLEM_CODE);
				Long problemcode = null;
				for(MCodeData problemcodes:problemCode){
					if(problemcodes.getmCodeValue().contains("Payment")){
						problemcode = problemcodes.getId();
					}
				}
				DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
				JSONObject ticketJson = new JSONObject();
				ticketJson.put("assignedTo", assigned);
				ticketJson.put("dateFormat", "dd MMMM yyyy");
				ticketJson.put("description", "Online Payment");
				ticketJson.put("locale", locale);
				ticketJson.put("priority", "LOW");
				ticketJson.put("problemCode", problemcode.intValue());
				ticketJson.put("ticketDate", df.format(DateUtils.getDateOfTenant()));
				ticketJson.put("ticketTime", " "+DateFormat.getTimeInstance().format(new Date()));
				ticketJson.put("ticketURL", "null");
				ticketJson.put("issue","Online Payment done by: "+clientId+" and PaymentId: "+paymentId+" amount: "+amount);
				ticketJson.put("status", "Open");
				ticketJson.put("departmentId", departmentId);
				this.ticketMasterApiResource.returnTicket(clientId,ticketJson.toString());
			
			}
		}
	}

	private String processOnlinePayment(String commandJson) {

		String deviceId = "", error = "", status = ConfigurationConstants.PAYMENTGATEWAY_SUCCESS, cardType = null, cardNumber = null, paymentType = null;
		Long statmentId = null;
		JsonArray deposit = new JsonArray();
		
		final JsonElement commandElement = this.fromApiJsonHelper.parse(commandJson);
		
		String currency = this.returnString("currency", commandElement);
		 
		  if(currency.equalsIgnoreCase("GBP") || currency.equalsIgnoreCase("GIP")) currency = "£";
		
		final Long clientId =  returnLongValue("clientId", commandElement);
		final String txnId =  this.returnString("transactionId", commandElement);
		final String amount =  this.returnString("total_amount", commandElement);
		final String source =  this.returnString("source", commandElement);
		final String data =  this.returnString("otherData", commandElement);
		
		if(this.fromApiJsonHelper.parameterExists("device", commandElement)){
			deviceId = this.returnString("device", commandElement);
		}
		if(this.fromApiJsonHelper.parameterExists("status", commandElement)){
			status = this.returnString("status", commandElement);
		}
		if(this.fromApiJsonHelper.parameterExists("error", commandElement)){
			error = this.returnString("error", commandElement);
		}
		
		if(this.fromApiJsonHelper.parameterExists("cardType", commandElement)){
			cardType = this.returnString("cardType", commandElement);
		}
		
		if(this.fromApiJsonHelper.parameterExists("cardNumber", commandElement)){
			cardNumber = this.returnString("cardNumber", commandElement);
		}
		if(this.fromApiJsonHelper.parameterExists("paymentType", commandElement)){
			paymentType = this.returnString("paymentType", commandElement);
			deposit = this.fromApiJsonHelper.extractJsonArrayNamed("deposit", commandElement);
		}
		if(this.fromApiJsonHelper.parameterExists("statmentId", commandElement)){
			statmentId = this.fromApiJsonHelper.extractLongNamed("statmentId", commandElement);
		}
		final BigDecimal totalAmount = new BigDecimal(amount);
		
		final Date date = DateUtils.getLocalDateOfTenant().toDate();
		
		final PaymentGateway paymentGateway = PaymentGateway.getPaymentGateway(deviceId, " ", date, totalAmount, txnId, source, data);
		
		if(status.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_PENDING)){
			paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_PENDING);
			paymentGateway.setRemarks(commandElement);
		}else if(status.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS)){
			paymentGateway.setStatus(status);
		}else if(status.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_COMPLETED)){
			status = ConfigurationConstants.PAYMENTGATEWAY_SUCCESS;
			paymentGateway.setStatus(status);
		}else{
			paymentGateway.setStatus(status);
			paymentGateway.setRemarks(error);
		}
		
		try {
			this.paymentGatewayRepository.save(paymentGateway);
		} catch (DataIntegrityViolationException dve) {
			paymentGateway.setStatus("Failure");
			paymentGateway.setRemarks(ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION + txnId + " in Payments");
			
			JsonObject withChanges = new JsonObject();
			withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_ALREADY_EXIST);
			withChanges.addProperty("description", ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION);
			withChanges.addProperty("amount", amount);
			withChanges.addProperty("obsPaymentId", "");
			withChanges.addProperty("transactionId", txnId);
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();
		}
		
	
		String returnOutput;
		
		if(status.equalsIgnoreCase("Success") || status.equalsIgnoreCase("Pending")){
			
			final String outputData = payment(clientId, paymentGateway.getId(), txnId, amount, error, paymentType, deposit, statmentId);
			
			final JsonElement paymentElement = this.fromApiJsonHelper.parse(outputData);
			final String result = this.returnString("result", paymentElement);
			final String description = this.returnString("description", paymentElement);
			
			if(currency.equalsIgnoreCase("£"))
			emailSending(clientId, result, description, txnId, currency + "" + amount, cardType, cardNumber);
			else emailSending(clientId, result, description, txnId, amount + " " + currency, cardType, cardNumber);
			
			returnOutput = outputData;
			
		} else{
			
			JsonObject object = new JsonObject();
			object.addProperty("result", status.toUpperCase());
			object.addProperty("description", error);
			object.addProperty("amount", totalAmount);
			object.addProperty("obsPaymentId", "");
			object.addProperty("transactionId", txnId);
			
			if(currency.equalsIgnoreCase("£"))
			emailSending(clientId, status, error, txnId, currency + "" + amount, cardType, cardNumber);
			else emailSending(clientId, status, error, txnId, amount + " " + currency, cardType, cardNumber);
			returnOutput = object.toString();
		}

		return returnOutput;
	}
	

	private String payment(Long clientId, Long id, String txnId, String amount, String errorDescription, String paymentType, JsonArray deposit, Long statmentId) {

		
		JsonObject withChanges = new JsonObject();
		
		try {
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			
			Long paymodeId = this.paymodeReadPlatformService.getOnlinePaymode("Online Payment");
			if (paymodeId == null) {
				paymodeId = Long.valueOf(83);
			}
			
			final BigDecimal totalAmount = new BigDecimal(amount);
			
			final String formattedDate = daformat.format(DateUtils.getLocalDateOfTenant().toDate());
			final JsonObject object = new JsonObject();
			object.addProperty("txn_id", txnId);
			object.addProperty("dateFormat", formatOfDate);
			object.addProperty("locale", "en");
			object.addProperty("amountPaid", totalAmount);
			object.addProperty("isChequeSelected", "no");
			object.addProperty("receiptNo", txnId);
			object.addProperty("remarks", "Payment Done");
			object.addProperty("paymentCode", paymodeId);

			if(paymentType != null) object.addProperty("paymentType", paymentType);
			object.add("deposit", deposit);
			if(statmentId != null) object.addProperty("statmentId", statmentId);

			if(paymentGateway.getStatus().equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS) || 
					paymentGateway.getStatus().equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_COMPLETED)){
				
				object.addProperty("paymentDate", formattedDate);
				
				   final JsonElement paymentElement = fromApiJsonHelper.parse(object.toString());
				   final CommandWrapper wrapper = new CommandWrapperBuilder().createPayment(clientId).withJson(object.toString()).build();
				   final JsonCommand command = new JsonCommand(null, object.toString(), paymentElement, fromApiJsonHelper, 
							"PAYMENT", clientId, null, null, null, null, null, null, null, null, null, wrapper.getHref());

					final CommandProcessingResult result = this.paymentWritePlatformService.createPayment(command);
				
				/*	final CommandWrapper commandRequest = new CommandWrapperBuilder().createPayment(clientId).withJson(object.toString()).build();
					CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);*/
					
				if (result !=null && result.resourceId() != Long.valueOf(-1)) {
					paymentGateway.setObsId(result.getClientId());
					paymentGateway.setPaymentId(result.resourceId().toString());
					paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
					paymentGateway.setRemarks("Payment Successfully completed..");
					paymentGateway.setAuto(false);
					
					AppUser maker=this.appUserRepository.findOne(1L);
					CommandSource commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
					commandSourceResult.updateResourceId(result.resourceId());
					commandSourceResult.updateClientId(result.getClientId());
					commandSourceResult.updateForAudit(result.getOfficeId(),result.getGroupId(), result.getClientId());
					this.commandSourceRepository.saveAndFlush(commandSourceResult);
					
					withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
					withChanges.addProperty("description", ConfigurationConstants.PAYMENT_SUCCESS_DESCRIPTION);
					withChanges.addProperty("amount", amount);
					withChanges.addProperty("obsPaymentId", result.resourceId().toString());
					withChanges.addProperty("transactionId", txnId);
					withChanges.addProperty("pgId", id);
					
				} else {
					paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
					paymentGateway.setRemarks("Payment is Not Processed..");
					
					withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
					withChanges.addProperty("description", ConfigurationConstants.PAYMENT_FAILURE_DESCRIPTION);
					withChanges.addProperty("amount", amount);
					withChanges.addProperty("obsPaymentId", "");
					withChanges.addProperty("transactionId", txnId);
					withChanges.addProperty("pgId", id);
				}
				
			} else if(paymentGateway.getStatus().equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_PENDING)){
				
				if(!paymentGateway.getSource().equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.PAYPAL)){
					
					EventAction eventAction=new EventAction(DateUtils.getDateOfTenant(), "Create Payment", "PAYMENT", EventActionConstants.EVENT_CREATE_PAYMENT,
							"/payments/"+clientId, id,object.toString(),null,clientId);	
					eventAction.updateStatus('P');
					this.eventActionRepository.save(eventAction);
				}
				
				withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_PENDING);
				if(null != errorDescription){
					withChanges.addProperty("description", errorDescription);	
				}else{
					withChanges.addProperty("description", ConfigurationConstants.PAYMENT_PENDING_DESCRIPTION);	
				}
				
				withChanges.addProperty("amount", amount);	
				withChanges.addProperty("obsPaymentId", "");	
				withChanges.addProperty("transactionId", txnId);
				withChanges.addProperty("pgId", id);
				
			}
			
			this.paymentGatewayRepository.save(paymentGateway);
			
			return withChanges.toString();
			
		} catch (ReceiptNoDuplicateException e) {
			
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			paymentGateway.setStatus("Failure");
			paymentGateway.setRemarks(ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION + txnId + " in Payments");
			
			withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_ALREADY_EXIST);
			withChanges.addProperty("description", ConfigurationConstants.PAYMENT_ALREADY_EXIST_DESCRIPTION);
			withChanges.addProperty("amount", amount);
			withChanges.addProperty("obsPaymentId", "");
			withChanges.addProperty("transactionId", txnId);
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();
		
		} catch (Exception e){
			
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			paymentGateway.setStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			paymentGateway.setRemarks(e.getMessage());
			
			withChanges.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			withChanges.addProperty("description", ConfigurationConstants.PAYMENT_ERROR_DESCRIPTION);
			withChanges.addProperty("amount", amount);
			withChanges.addProperty("obsPaymentId", "");
			withChanges.addProperty("transactionId", txnId);
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();
		}
	}
	
	private void emailSending(final Long clientId, String result,
			String description, final String txnId, final String amount,
			final String cardType, final String cardNumber) {

		Client client = this.clientRepository.findOne(clientId);
		if (client == null) {
			throw new ClientNotFoundException(clientId);
		}

		if (client.getEmail() == null || client.getEmail().isEmpty()) {
			throw new EmailNotFoundException(clientId);
		}

		Configuration configuration = this.configurationRepository
				.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_PAYMENT_EMAIL_DESC);

		if (configuration != null && configuration.isEnabled() && configuration.getValue() != null) {

			final JsonElement element = this.fromApiJsonHelper.parse(configuration.getValue());
			final JsonArray jsonArray = element.getAsJsonArray();

			for (JsonElement jsonElement : jsonArray) {
				String value = returnString("value", jsonElement);

				if (result.equalsIgnoreCase(value)) {
					result = returnString("result", jsonElement);
					description = returnString("response", jsonElement);
					break;
				}
			}
		}

		if (null == messageDetails) {
			messageDetails = this.billingMessageTemplateRepository
					.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PAYMENT_RECEIPT);
		}

		if (messageDetails != null) {

			String subject = messageDetails.getSubject();
			String body = messageDetails.getBody();
			String header = messageDetails.getHeader();
			String footer = messageDetails.getFooter();

			/*header = header.replace("<PARAM1>", (client.getDisplayName() == null) || 
					(client.getDisplayName() == "") ? client.getFirstname() + client.getLastname() : client.getDisplayName());*/
			header = header.replace("<PARAM1>", client.getFirstname());
			
			if(result.equalsIgnoreCase("Failed") || result.equalsIgnoreCase("Failed-")){
				String color="red";
				body = body.replace("<PARAM2>", "<strong><font color="+color+">"+result+"</font></strong>");
			}else{
				body = body.replace("<PARAM2>", result);
			}
			
			//body = body.replace("<PARAM2>", result);
			body = body.replace("<PARAM3>", description);
			body = body.replace("<PARAM4>", amount);
			body = body.replace("<PARAM5>", txnId);

			if (body.contains("<PARAM6>") && cardType != null) {
				body = body.replace("<PARAM6>", cardType);
			}

			if (body.contains("<PARAM7>") && cardNumber != null) {
				body = body.replace("<PARAM7>", cardNumber);
			}

			final BillingMessage billingMessage = new BillingMessage(header, body, footer,
					client.getOffice().getOfficeAddress().getEmail(), client.getEmail(),
					subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, 
					BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

			this.messageDataRepository.save(billingMessage);
		} else {
			throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PAYMENT_RECEIPT);
		}
	}
	
	private static String processPostNetellerRequests(String url, String encodePassword, String data, 
			String authenticationType) throws ClientProtocolException, IOException, JSONException {
		
		@SuppressWarnings("resource")
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(url);
		
		String authHeader = authenticationType.trim() + " " + encodePassword;
		StringEntity se = new StringEntity(data.trim());
		
		postRequest.setHeader("Authorization", authHeader);
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setEntity(se);
		
		HttpResponse response = httpClient.execute(postRequest);
		
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		
		String output="",output1="";

		if (response.getStatusLine().getStatusCode() == 404) {
			
			System.out.println("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode());
			return "failure : errorCode:404 ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			
			System.out.println(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode());
			return "failure : errorCode:401 AuthenticationException";

		} else if (response.getStatusLine().getStatusCode() != 200) {
			
			System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			
			while ((output = br.readLine()) != null) {
				output1 = output1 + output;
			}
			JSONObject obj = new JSONObject(output1).getJSONObject("error");
			String message = obj.getString("message");
			br.close();
			return "failure : Error Output="+message;
		
		} else{
			
			while ((output = br.readLine()) != null) {
				output1 = output1 + output;
			}
			
			br.close();
			
			return output1;
		}
	}
	
	private String validatingNetellerOutput(String tokenOutput) {
		
		if(tokenOutput.contains("failure :")){
			return tokenOutput;
		}else{
			return ConfigurationConstants.PAYMENTGATEWAY_SUCCESS;
		}
		
	}

	@Override
	public String createFingerPrint(BigDecimal amount) {
		

			final String pgConfigValue = this.paymentGatewayConfigurationRepositoryWrapper
					.getValue(ConfigurationConstants.AUTHORIZENET_PAYMENTGATEWAY);
			
			JsonElement parsedJson = this.fromApiJsonHelper.parse(pgConfigValue);
			String apiLoginID = this.returnString("merchantId", parsedJson);
			String transactionKey = this.returnString("transactionKey", parsedJson);
			String length = this.returnString("serialNoLength", parsedJson);
			String generatedKey = RandomStringUtils.randomNumeric(Integer.valueOf(length));
			String amount1 = String.valueOf(amount);
			Fingerprint fingerprint = Fingerprint.createFingerprint(apiLoginID,transactionKey, Long.valueOf(generatedKey), amount1);
			long x_fp_sequence = fingerprint.getSequence();
			long x_fp_timestamp = fingerprint.getTimeStamp();
			String x_fp_hash = fingerprint.getFingerprintHash();

			JsonObject jsonobject = new JsonObject();
			jsonobject.addProperty("x_merchantId", apiLoginID);
			jsonobject.addProperty("x_fp_sequence", x_fp_sequence);
			jsonobject.addProperty("x_fp_timestamp", x_fp_timestamp);
			jsonobject.addProperty("x_fp_hash", x_fp_hash);
			jsonobject.addProperty("x_amount", amount1);

			return jsonobject.toString();
		}

	@SuppressWarnings("static-access")
	@Override
	public String echeckProcess(String apiRequestBodyAsJson) {
		
		this.paymentGatewayCommandFromApiJsonDeserializer.validateForEcheckPayment(apiRequestBodyAsJson);
		
		final String pgConfigValue = this.paymentGatewayConfigurationRepositoryWrapper
				.getValue(ConfigurationConstants.ECHECK_PAYMENTGATEWAY);
		
		final JsonElement jsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		final String bankName = this.returnString("bankName", jsonElement);
		final String accountNumber = this.returnString("accountNumber", jsonElement);
		final String bankAccountTypeValue = this.returnString("bankAccountType", jsonElement);
		final String routingNumber = this.returnString("routingNumber", jsonElement);
		final BigDecimal amount = this.returnBigDecimal("amount", jsonElement);
		final Long clientId = returnLongValue("clientId", jsonElement);
		final String currency = this.returnString("currency", jsonElement);
		final String locale = this.returnString("locale", jsonElement);
		final String checkNumber = this.returnString("checkNumber", jsonElement);
		final String emailId = this.returnString("emailId", jsonElement);
		
		final JsonElement element = this.fromApiJsonHelper.parse(pgConfigValue);
		final String merchantId = this.returnString("merchantId", element);
		final String transactionKey = this.returnString("transactionKey", element);
		final String environmentValue = this.returnString("environment", element);
		final String transactionTypeValue = this.returnString("transactionType", element);
		final String eCheckTypeValue = this.returnString("eCheckType", element);
		
		Environment environment = Environment.SANDBOX;
		
		if(environmentValue.equalsIgnoreCase("SANDBOX")) {
			environment = Environment.SANDBOX;
		} else if (environmentValue.equalsIgnoreCase("PRODUCTION")) {
			environment = Environment.PRODUCTION;
		} else {
			environment = Environment.CUSTOM;
		}
		
	    final Merchant merchant = Merchant.createMerchant(environment, merchantId, transactionKey);   

        // create transaction
	    final TransactionType transactionType = findByValue(transactionTypeValue);
	    final Transaction authCaptureTransaction = merchant.createAIMTransaction
                (transactionType, amount);
     
	    final ECheck eCheck = ECheck.createECheck();
	    final ECheckType eCheckType = ECheckType.findByValue(eCheckTypeValue);
	    final BankAccountType bankAccountType = BankAccountType.findByValue(bankAccountTypeValue);
        
		eCheck.setBankAccountName(bankName);        
		eCheck.setBankAccountNumber(accountNumber);
		eCheck.setBankAccountType(bankAccountType);
		eCheck.setBankName(bankName);
		eCheck.setECheckType(eCheckType);
		eCheck.setRoutingNumber(routingNumber);
		if(null != checkNumber) {
			eCheck.setBankCheckNumber(checkNumber);
		}
		
		ConfigurationPropertyData configurationPropertyData = this.configReadPlatformService.retrieveGlobalConfigurationByName(ConfigurationConstants.CONFIG_IS_ECHECK_EMAIL_SEND);
		BillingMessageTemplate echeckMessageDetails = this.billingMessageTemplateRepository
				.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_ECHECK_EMAIL);
		OrderNotificationData orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
		final EmailReceipt emailReceipt = EmailReceipt.createEmailReceipt();
		emailReceipt.setEmail(emailId);
		emailReceipt.setEmailCustomer(configurationPropertyData.isEnabled());
		emailReceipt.setHeaderEmailReceipt(echeckMessageDetails.getHeader());
		emailReceipt.setFooterEmailReceipt(echeckMessageDetails.getFooter());
		emailReceipt.setMerchantEmail(orderData.getOfficeEmail() == null ? orderData.getEmailId() : orderData.getOfficeEmail());
		
		authCaptureTransaction.setECheck(eCheck);
		authCaptureTransaction.setEmailReceipt(emailReceipt);
		
		
		@SuppressWarnings("unchecked")
		final Result<Transaction> result = (Result<Transaction>)merchant.postTransaction(authCaptureTransaction);

		final Transaction value = result.getTarget();
          System.out.println(result.getResponseText());
		final Map<ResponseField, String> responseMap =  value.getResponseMap();
          
		String status = "", transactionId = "";
		
		if(result.isApproved()) {
			status = ConfigurationConstants.PAYMENTGATEWAY_SUCCESS;
			transactionId = value.getTransactionId();
        } 
        else if (result.isDeclined()) {
        	String date = new SimpleDateFormat("ddmmyyyyHHmmss").format(new Date());
        	transactionId = date + new RandomStringUtils().random(8);
        	status = ConfigurationConstants.PAYMENTGATEWAY_ALREADY_EXIST;
        }
        else {
        	String date = new SimpleDateFormat("ddmmyyyyHHmmss").format(new Date());
        	transactionId = date + new RandomStringUtils().random(8);
        	status = ConfigurationConstants.PAYMENTGATEWAY_FAILURE;
        }
		
		JsonObject object = new JsonObject();
		object.addProperty("transactionId", transactionId);
		object.addProperty("error", result.getResponseText());
		object.addProperty("status", status);
		object.addProperty("total_amount", geBigDecimalValue(responseMap));
		object.addProperty("otherData", geValue(responseMap));
		object.addProperty("currency", currency);
		object.addProperty("clientId", clientId);
		object.addProperty("source", geValue("x_method", responseMap));
		object.addProperty("locale", locale);
		
        return object.toString();
        
	}
	
	
	public TransactionType findByValue(String value) {
		for(TransactionType echeckType : TransactionType.values()) {
			if(echeckType.getValue().equals(value)) {
				return echeckType;
			}
		}
		return TransactionType.VOID;
	}
	
	private BigDecimal geBigDecimalValue(final Map<ResponseField, String> responseMap){	
		
		ResponseField field = ResponseField.findByFieldName("x_amount");
		String amount = responseMap.get(field);
		return new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	private String geValue(final String parameterName, final Map<ResponseField, String> responseMap){
		ResponseField field = ResponseField.findByFieldName(parameterName);
		return responseMap.get(field);
	}
	
	private String geValue(final Map<ResponseField, String> responseMap){		
		
		JsonObject object = new JsonObject();
		object.addProperty("accountNunber", geValue("x_account_number", responseMap));
		object.addProperty("responseCode", geValue("x_response_code", responseMap));
		object.addProperty("method", geValue("x_method", responseMap));
		object.addProperty("responseReasonText", geValue("x_response_reason_text", responseMap));
		object.addProperty("transactionType", geValue("x_type", responseMap));
		object.addProperty("cardType", geValue("x_card_type", responseMap));
		
		return object.toString();
	}

	@Override
	public CommandProcessingResult crediCardProcess(JsonCommand command) {
		try {

			context.authenticatedUser();
			String creditcardRespone = null;
			Long resourceid = null;
			final JsonElement commandElement = this.fromApiJsonHelper.parse(command.json());
			final Merchant merchant = getMerchant();
			MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
			
			ApiOperationBase.setEnvironment(merchant.getEnvironment());
			merchantAuthenticationType.setName(merchant.getLogin());
			merchantAuthenticationType.setTransactionKey(merchant.getTransactionKey());
			
			ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

			PaymentType paymentType = new PaymentType();
			CreditCardType creditCard = new CreditCardType();

			final String cardNumber = this.fromJsonHelper.extractStringNamed("cardnumber", commandElement);
			final String expirydate = this.fromJsonHelper.extractStringNamed("expirationdate", commandElement);
			creditCard.setCardNumber(cardNumber);
			creditCard.setExpirationDate(expirydate);
			paymentType.setCreditCard(creditCard);

			final String amount = this.fromJsonHelper.extractStringNamed("totalamount", commandElement);

			TransactionRequestType txnRequest = new TransactionRequestType();
			
			txnRequest.setTransactionType(TransactionTypeEnum.AUTH_ONLY_TRANSACTION.value());
			txnRequest.setPayment(paymentType);
			txnRequest.setAmount(new BigDecimal(amount).setScale(2,RoundingMode.CEILING));
			NameAndAddressType customer = new NameAndAddressType();
			customer.setFirstName( this.fromJsonHelper.extractStringNamed("name",commandElement));
			txnRequest.setShipTo(customer);
			

			// Make the API Request
			CreateTransactionRequest apiRequest = new CreateTransactionRequest();
			apiRequest.setTransactionRequest(txnRequest);
			CreateTransactionController controller = new CreateTransactionController(apiRequest);
			controller.execute();
			CreateTransactionResponse response = controller.getApiResponse();
			JsonObject object = new JsonObject();
			if (response != null) {
				if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
					
					TransactionResponse result = response.getTransactionResponse();
					if (result.getResponseCode().equals("1")) {
						object.addProperty("response", ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
						object.addProperty("cardtype", result.getAccountType());
						object.addProperty("transId", result.getTransId());
						creditcardRespone = object.toString();
						resourceid = Long.parseLong(result.getTransId());
					} else {
						List<Error> errorMessage = response.getTransactionResponse().getErrors().getError();
						object.addProperty("response", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
						object.addProperty("errormessage", errorMessage.get(0).getErrorText());
						creditcardRespone =object.toString();
						resourceid = 0L;
					}
					return new CommandProcessingResult(creditcardRespone, 0L);
					
				} else {

					List<Error> errorMessage = response.getTransactionResponse().getErrors().getError();
					object.addProperty("response", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
					object.addProperty("errormessage", errorMessage.get(0).getErrorText());
					creditcardRespone =object.toString();
					return new CommandProcessingResult(creditcardRespone, 0L);
				}

			} else {
				object.addProperty("response", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
				creditcardRespone =object.toString();
				return new CommandProcessingResult(creditcardRespone, 0L);
			}

		} catch (Exception e) {

			return CommandProcessingResult.empty();
		}

	}

	private Merchant getMerchant() {

		final String value = this.paymentGatewayConfigurationRepositoryWrapper
				.getValue(ConfigurationConstants.AUTHORIZENET_PAYMENTGATEWAY);
		
		final JsonElement element = this.fromJsonHelper.parse(value);
		final String apiLogin = this.fromJsonHelper.extractStringNamed("merchantId", element);
		final String transactionKey = this.fromJsonHelper.extractStringNamed("transactionKey", element);
		final String environment = this.fromJsonHelper.extractStringNamed("environment", element);
		final String currency = this.fromJsonHelper.extractStringNamed("currency", element);

		if(currency != null) {
			this.currency = currency;
		}
		
		Environment environmentVal = Environment.SANDBOX;

		if (environment.equalsIgnoreCase(ConfigurationConstants.PRODUCTION))
			environmentVal = Environment.PRODUCTION;

		return Merchant.createMerchant(environmentVal, apiLogin, transactionKey);
	}

	@Override
	public CommandProcessingResult createOrderWorldpay(final JsonCommand command) {
		
		final JsonElement element = fromApiJsonHelper.parse(command.json());
		System.out.println(command.json().toString());
		
		Locale locale=Locale.ENGLISH;
		BigDecimal amount =returnBigDecimal("totalamount", element);
		
		//Double damount=amount.doubleValue();
		//System.out.println("double  amount"+damount);
		//Double totalamount=damount*100;
		
		BigDecimal totalamount=amount.multiply(new BigDecimal(100));
		
		Long clientId = returnLongValue("clientId", element);
		String auth = returnString("auth", element);
		
		Long statementId = null;
		
		if(this.fromApiJsonHelper.parameterExists("statementId", element)){
			statementId = returnLongValue("statementId", element);
		}
			
		try {
			context.authenticatedUser();
			
			/**getting configuration details*/
			final String value = this.paymentGatewayConfigurationRepositoryWrapper
					.getValue(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY);
			final JsonElement configelement = this.fromJsonHelper.parse(value);
			final String service_key = this.fromJsonHelper.extractStringNamed("service_key", configelement);
			
			/**this id for get client Data */
			ClientData ClientData= this.clientReadPlatformService.retrieveOne(clientId);
			
			/**this id for get token */
			String reultResponce=GetToken(command.json());
			logger.info("*********** Token Responce *******"+reultResponce.toString());
			JSONObject jsonObj = new JSONObject(reultResponce.toString());
			
			if(jsonObj.has("token") && jsonObj.has("reusable") && jsonObj.has("paymentMethod")){
				String gettoken=jsonObj.getString("token");
				Boolean reusable=jsonObj.getBoolean("reusable");
				if(reusable){
					jsonObj.put("r_type", "RECURRING");
				}
				String paymentMethod=jsonObj.getString("paymentMethod");
				JSONObject paymentMethodJson = new JSONObject(paymentMethod.toString());
				String Ctype=paymentMethodJson.getString("type");
				String name=paymentMethodJson.getString("name");
				
				//Ctype="ObfuscatedCard"
				jsonObj.put("type", Ctype);
				jsonObj.put("name", name);
				jsonObj.put("paymentMethod", paymentMethodJson);
				jsonObj.put("isWorldpayBilling", reusable);
				
			WorldpayRestClient restClient = new WorldpayRestClient(service_key);
			
			String orderDescription;
			String OrderCode;
			
			if(null!=statementId){
				OrderCode="S/"+clientId.toString()+"/"+statementId;
			}else{
				OrderCode="S/"+clientId.toString();
			}
			
			boolean authorizeOnly=false;
			if(jsonObj.getBoolean("reusable") && auth.equalsIgnoreCase("Y")){
				 authorizeOnly=true;
				orderDescription ="Authorization -"+clientId.toString();
			}else{
				 orderDescription ="Online Payment-"+OrderCode;
			}
			
			OrderRequest orderRequest = new OrderRequest();
			orderRequest.setToken(gettoken);
			orderRequest.setAuthorizeOnly(authorizeOnly);
			
			if(authorizeOnly){
			 OrderCode="A/"+clientId.toString();
			}
			orderRequest.setAmount(totalamount.intValue());
			orderRequest.setCurrencyCode(CurrencyCode.GBP);
			orderRequest.setName(name);
			//orderRequest.setIs3DSOrder(true);
			orderRequest.setOrderDescription(orderDescription);
			orderRequest.setCustomerOrderCode(OrderCode);
			orderRequest.setShopperEmailAddress(ClientData.getEmail());
			
			
			Address address = new Address();
			address.setAddress1(ClientData.getAddressNo());
			address.setAddress2(ClientData.getCity());
			address.setCity(ClientData.getCity());
			address.setCountryCode(CountryCode.GB);
			if(ClientData.getZip() !=null){
				address.setPostalCode(ClientData.getZip());
			}else {
				address.setPostalCode("GX11 1AA");
			}
			address.setPostalCode(ClientData.getZip());
			orderRequest.setBillingAddress(address);

			OrderResponse orderResponse = restClient.getOrderService().create(orderRequest);
			
			logger.info("********** Order Responce**** \n" +
					   "*********** Status *****"+orderResponse.getPaymentStatus()+"****** \n" +
					   "******Amount ********"+orderResponse.getAmount()+"***********" +
					   "****** orderCode *********"+orderResponse.getOrderCode()+"********\n" +
					   "*******");
			JsonObject resultobject = new JsonObject();
			resultobject.addProperty("status", orderResponse.getPaymentStatus());
			resultobject.addProperty("transId", orderResponse.getOrderCode());
			
			Double a=orderResponse.getAmount().doubleValue();
			Double tamount=a/100;
			
			resultobject.addProperty("amount", tamount);
			if(orderResponse.getPaymentStatus().equalsIgnoreCase("SUCCESS") && reusable || orderResponse.getPaymentStatus().equalsIgnoreCase("AUTHORIZED")){
				
				final Client client = this.clientRepository.findOne(clientId);
				ClientCardDetails clientCardDetails= this.clientCardDetailsRepository.findOneByClientObfuscatedCard(client);
				if(clientCardDetails!=null){
					
					logger.info("****geting old card details ............."+clientCardDetails.getW_token());
					//clientCardDetails.setIsDeleted('Y');
					this.clientCardDetailsRepository.delete(clientCardDetails);
					//clientCardDetails.setW_token(gettoken);
					//clientCardDetails.setR_type("RECURRING");
					//clientCardDetails.setClient(client);
					//this.clientCardDetailsRepository.saveAndFlush(clientCardDetails);
					String re=this.clientCardDetailsApiResource.createClientIdentifier(clientId, jsonObj.toString());
					logger.info("****Update status ..if.............."+re);
				}else{
					String re=this.clientCardDetailsApiResource.createClientIdentifier(clientId, jsonObj.toString());
					logger.info("****Update status .........else.........."+re);
				}
				//String re=this.clientCardDetailsApiResource.createClientIdentifier(clientId, jsonObj.toString());
				JsonObject statusjson=new JsonObject();
				statusjson.addProperty("isWorldpayBilling", reusable);
				String updateStatus=this.selfCareApiResource.selfCareKortaTokenStore(clientId, statusjson.toString());
				logger.info("****Update status ....isworldpay*********"+updateStatus.toString());
		       }else if(orderResponse.getPaymentStatus().equalsIgnoreCase("SUCCESS") && !reusable){
				
		    		 /*final Client client = this.clientRepository.findOne(clientId);
		    		 ClientCardDetails clientCardDetails= this.clientCardDetailsRepository.findOneByClientObfuscatedCard(client);
		    		 if(clientCardDetails!=null){
		    			 clientCardDetails.setR_type("ECOM");
		    			 clientCardDetails.setIsDeleted('Y');
		    			 JsonObject statusjson=new JsonObject();
		 				 statusjson.addProperty("isWorldpayBilling", reusable);
		 				 String updateStatus=this.selfCareApiResource.selfCareKortaTokenStore(clientId, statusjson.toString());
		    			this.clientCardDetailsRepository.saveAndFlush(clientCardDetails);
		    			logger.info("****Update status ....isworldpay Card details ****By Default *****"+updateStatus.toString()+"*********"+reusable);
		    		 }
		    		 JsonObject statusjson=new JsonObject();
	 				 statusjson.addProperty("isWorldpayBilling", reusable);
	 				 String updateStatus=this.selfCareApiResource.selfCareKortaTokenStore(clientId, statusjson.toString());*/
	 				//logger.info("****Update status ....isworldpay Status ****By Default *****"+updateStatus.toString()+""+reusable);
	 				logger.info("****Update status ....isworldpay Status ****By Default *****");
		       }
			return new CommandProcessingResult(resultobject.toString(), clientId);
			
		}if(jsonObj.has("customCode") && jsonObj.has("message")){	
			JsonObject object = new JsonObject();
			object.addProperty("customCode", jsonObj.getString("customCode"));
			object.addProperty("message", jsonObj.getString("message"));
			object.addProperty("status", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			object.addProperty("transId", "");
			
			String chars = "0123456789";
		    int string_length = 4;
			String randomstring = "";
			for (int i=0; i<string_length; i++) {
				int rnum = (int) Math.floor(Math.random() * chars.length());
				randomstring += chars.substring(rnum,rnum+1);	
			}	
			
			Double ramount=totalamount.doubleValue();
			Double famount=ramount/100;
			    JsonObject errorJson=new JsonObject();
			    errorJson.addProperty("status", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			    errorJson.addProperty("locale", locale.toString());
			    errorJson.addProperty("source", ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY);
			    errorJson.addProperty("total_amount", famount);
			    errorJson.addProperty("error", jsonObj.getString("customCode")+"-"+jsonObj.getString("message"));
			    errorJson.addProperty("otherData", jsonObj.getString("customCode")+jsonObj.getString("message"));
			    errorJson.addProperty("transactionId", randomstring+"-"+clientId);
			    errorJson.addProperty("currency", CurrencyCode.GBP.toString());
			    errorJson.addProperty("clientId", clientId);
			    
			    String result= onlinePaymentGateway(errorJson.toString());
			    logger.info("******* Exception occure one of payment by token ******"+result.toString()); 
			    
			return new CommandProcessingResult(object.toString(), clientId);
			
		}else{
			
		}//end else 
			
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(element, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}catch (UnsupportedEncodingException dve) {
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}catch(WorldpayException wpe){
			logger.info("******* Exception By Worldpay***********"+wpe.getMessage());
		    throw new PlatformDataIntegrityException(wpe.getMessage(), "Error",wpe.getMessage());
		}
		catch (Exception e) {
		     e.getMessage();
		     logger.info("******* Exception Error ***********"+e.getMessage());
	   }
		return null;
	}

	@SuppressWarnings("resource")
	private String GetToken(String command) throws UnsupportedEncodingException, JSONException {
		
		/**getting configuration details*/
		final String value = this.paymentGatewayConfigurationRepositoryWrapper
				.getValue(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY);
		
		final JsonElement configelement = this.fromJsonHelper.parse(value);
		final String service_key = this.fromJsonHelper.extractStringNamed("service_key", configelement);
		final String client_key = this.fromJsonHelper.extractStringNamed("client_key", configelement);
		
		JSONObject obj=new JSONObject(command.toString());
		
		String cardNumber=obj.getString("cardNumber");
		String expiryMonth=obj.getString("expiryMonth");
		String cvc=obj.getString("cvc");
		String expiryYear=obj.getString("expiryYear");
		String name=obj.getString("name");
		String type=obj.getString("type");
		boolean reusable=obj.getBoolean("reusable");
		
		HttpPost post = new HttpPost(baseurl+"/tokens");
		post.addHeader("Content-Type", "application/json");
		post.addHeader("Authorization",service_key);
		JsonObject jsonobj = new JsonObject();
		jsonobj.addProperty("reusable", reusable);
		JsonObject paymentMethod = new JsonObject();
		
		paymentMethod.addProperty("name",name);
		paymentMethod.addProperty("expiryMonth", expiryMonth);
		paymentMethod.addProperty("expiryYear",expiryYear);
		//paymentMethod.addProperty("issueNumber",1);
		paymentMethod.addProperty("cardNumber",cardNumber);
		paymentMethod.addProperty("type", "Card");
		paymentMethod.addProperty("cvc", cvc);
		
		jsonobj.add("paymentMethod", paymentMethod);
		//jsonobj.addProperty("clientKey", "T_C_527ad219-b537-47a1-a6f3-b51e29158dae");
		jsonobj.addProperty("clientKey", client_key);
		logger.info("**********Sending json datat to get Token******** "+jsonobj.toString()+"\n");
		StringEntity se = new StringEntity(jsonobj.toString());
		post.setEntity(se);
		try {
			
			
			SSLContext ctx = SSLContext.getInstance("TLSv1.2");
			ctx.init(null,null,null);
			SSLContext.setDefault(ctx);
			
			HttpResponse response = new DefaultHttpClient().execute(post);
			InputStream is = response.getEntity().getContent();
		BufferedReader	bufferedReader = new BufferedReader(new InputStreamReader(is));
			String line, result = "";
			while ((line = bufferedReader.readLine()) != null) {
				result += line;
				logger.info("**********Get Token Responce ******** "+result);
			}
		return result;
		} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public CommandProcessingResult createOrderWorldpayRecurring(Long clientId,
			String apiRequestBodyAsJson) {
		
			final JsonElement element = fromApiJsonHelper.parse(apiRequestBodyAsJson);
			Locale locale=Locale.ENGLISH;
			String gettoken=this.fromJsonHelper.extractStringNamed("gettoken", element);
			BigDecimal amount=this.fromApiJsonHelper.extractBigDecimalNamed("totalamount", element, locale);
			
			//Double amount1=amount.doubleValue();
			BigDecimal totalamount=amount.multiply(BigDecimal.valueOf(100));
			
			String orderType=this.fromJsonHelper.extractStringNamed("orderType", element);
			String orderCode=this.fromJsonHelper.extractStringNamed("orderCode", element);
			String name=this.fromJsonHelper.extractStringNamed("name", element);
			
			/**getting configuration details*/
			final String value = this.paymentGatewayConfigurationRepositoryWrapper
					.getValue(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY);
			final JsonElement configelement = this.fromJsonHelper.parse(value);
			final String service_key = this.fromJsonHelper.extractStringNamed("service_key", configelement);
			
			/**this id for get client Data */
			List<AddressData> ClientData=this.addressReadPlatformService.retrieveClientAddressDetails(clientId);
			
			if(orderType.equalsIgnoreCase("RECURRING"));
			WorldpayRestClient restClient = new WorldpayRestClient(service_key);
			OrderRequest orderRequest = new OrderRequest();
			orderRequest.setToken(gettoken);
			orderRequest.setAmount(totalamount.intValue());
			orderRequest.setCurrencyCode(CurrencyCode.GBP);
			orderRequest.setName(name);
			String ordercode1="R/"+clientId.toString()+"/"+orderCode;
			orderRequest.setCustomerOrderCode(ordercode1);
			orderRequest.setOrderDescription("Recurring Payment-"+orderCode);
			orderRequest.setOrderType(orderType);
			 
			Address address = new Address();
			address.setAddress1(ClientData.get(0).getAddressNo());
			address.setAddress2(ClientData.get(0).getState());
			address.setCity(ClientData.get(0).getCity());
			address.setCountryCode(CountryCode.GB);
			address.setPostalCode(ClientData.get(0).getZip());
			orderRequest.setBillingAddress(address);
			try {
				
				RecurringBillingHistory recurringBillingHistory = new RecurringBillingHistory();
				
			    OrderResponse orderResponse = restClient.getOrderService().create(orderRequest);
			    
			    if(orderResponse.getPaymentStatus().equalsIgnoreCase("SUCCESS")){
			      String resultrespnc=orderResponse.getPaymentResponse().toString();
			      JsonObject object = new JsonObject();
					object.addProperty("source", ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
					
					
					Double resultamount=orderResponse.getAmount().doubleValue();
					Double finalamount=resultamount/100;
					
					/*int respamount=orderResponse.getAmount();
					Double doubleAmount=(double) (respamount/100);*/
					//BigDecimal bigamount= new BigDecimal(doubleAmount);
					
					object.addProperty("total_amount", finalamount);
					object.addProperty("currency", orderResponse.getCurrencyCode().toString());
					object.addProperty("clientId", clientId);
					object.addProperty("statmentId", orderCode);
					object.addProperty("cardType", "");
					object.addProperty("cardNumber", "");
					object.addProperty("locale", "en");
					object.addProperty("transactionId", orderResponse.getOrderCode());
					
					String transStatus = "Success";
				    object.addProperty("status", transStatus);
				    object.addProperty("otherData", resultrespnc);
				    logger.info("*********Order Sending To Online Pay in OBS*********"+object.toString());
				    String result= onlinePaymentGateway(object.toString());
				    logger.info("--------Updating Payment details in Local Sucessfully with client id....."+clientId+"........\n" +
				    		"----Order code -----"+orderResponse.getOrderCode()+"....."+result);
				    
				    recurringBillingHistory.setClientId(clientId);
				    recurringBillingHistory.setSource(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    recurringBillingHistory.setTransactionStatus(transStatus); //status
				   // recurringBillingHistory.setObsStatus(transStatus); //status
					//recurringBillingHistory.setTransactionId(randomstring+"-"+clientId);
					recurringBillingHistory.setTransactionId(orderResponse.getOrderCode());
					recurringBillingHistory.setTransactionDate(DateUtils.getDateOfTenant());
					recurringBillingHistory.setTransactionCategory(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				   //recurringBillingHistory.setObsDescription(null);
				    recurringBillingHistory.setOrderId(null);
				    recurringBillingHistory.setTransactionData(resultrespnc);
				    
				    this.recurringBillingHistoryRepository.save(recurringBillingHistory);
				    
		    logger.info("-------Updated in Recurring Histroy----------------"+clientId+"------"+transStatus+"----------");
				    
			    }else if(!orderResponse.getPaymentStatus().equalsIgnoreCase("SUCCESS")){
			    	
			    	  System.out.println("Recurring Payment Status............"+orderResponse.getPaymentStatus());
			    	  String status="Failed";
			    	 
			    	  String chars = "012345678";
					    int string_length = 5;
						String randomstringnew = "";
						for (int i=0; i<string_length; i++) {
							int rnum = (int) Math.floor(Math.random() * chars.length());
							randomstringnew += chars.substring(rnum,rnum+1);	
						}	
						
				    	
					    	Double ramount=totalamount.doubleValue();
							Double famount=ramount/100;
						    JsonObject errorJson=new JsonObject();
						    errorJson.addProperty("status", status);
						    errorJson.addProperty("locale", locale.toString());
						    errorJson.addProperty("source", ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
						    errorJson.addProperty("total_amount", famount);
						    errorJson.addProperty("error", orderResponse.getPaymentStatus()+"--"+orderResponse.getPaymentStatusReason());
						    errorJson.addProperty("otherData", orderResponse.getPaymentStatusReason());
						    errorJson.addProperty("transactionId", randomstringnew+"-"+clientId);
						    errorJson.addProperty("currency", CurrencyCode.GBP.toString());
						    errorJson.addProperty("clientId", clientId);
				    logger.info("-----Error Json sendi to UI-----"+errorJson.toString());
				    
				    
				    recurringBillingHistory.setClientId(clientId);
				    recurringBillingHistory.setSource(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    recurringBillingHistory.setTransactionStatus(status); //status
				    // recurringBillingHistory.setObsStatus(null); //status
					//recurringBillingHistory.setTransactionId(randomstring+"-"+clientId);
					recurringBillingHistory.setTransactionId(orderResponse.getOrderCode());
					recurringBillingHistory.setTransactionDate(DateUtils.getDateOfTenant());
					recurringBillingHistory.setTransactionCategory(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    recurringBillingHistory.setObsDescription(orderResponse.getPaymentStatusReason());
				    recurringBillingHistory.setOrderId(null);
				    recurringBillingHistory.setTransactionData(orderResponse.toString());
				    
				    this.recurringBillingHistoryRepository.save(recurringBillingHistory);
				    
				    logger.info("-----"+orderResponse.getPaymentStatus()+"------Error Occure ----Update Recuuring  Histriy ---" +
				    		"--Recurring Payments by client id ---"+clientId+"-----------");
				    
				    
				    String errorresult= onlinePaymentGateway(errorJson.toString());
						    
			    	logger.info("Error Occure ---------Recurring Payments Status-------ClientId------"+clientId+"-----------"+orderResponse.getPaymentStatus());
			    	logger.info("Error Occure ---------Recurring Payments-----------"+errorresult);
			    	logger.info("Error Occure ---------Recurring Payments by client id ---"+clientId+"-----------"+errorresult.toString());
			    	
			    	/*recurringBillingHistory.setClientId(clientId);
				    recurringBillingHistory.setSource(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    recurringBillingHistory.setTransactionStatus(status); //status
				    // recurringBillingHistory.setObsStatus(null); //status
					//recurringBillingHistory.setTransactionId(randomstring+"-"+clientId);
					recurringBillingHistory.setTransactionId(orderResponse.getOrderCode());
					recurringBillingHistory.setTransactionDate(DateUtils.getDateOfTenant());
					recurringBillingHistory.setTransactionCategory(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    recurringBillingHistory.setObsDescription(orderResponse.getPaymentStatusReason());
				    recurringBillingHistory.setOrderId(null);
				    recurringBillingHistory.setTransactionData(orderResponse.toString());
				    
				    this.recurringBillingHistoryRepository.save(recurringBillingHistory);*/
				    logger.info("-----------Error Occure ----Update Recuuring  Histriy -----Recurring Payments by client id ---"+clientId+"-----------"+errorresult.toString());
				    
			    }
			} catch (WorldpayException e) {
				
				 RecurringBillingHistory recurringBillingHistory = new RecurringBillingHistory();
				 
				logger.info("***Error Occure in worldpay Recurring payments Please check creditials****\n");
				logger.info("----Error code: "+ e.getApiError().getCustomCode());
				logger.info("----Error description: "+e.getApiError().getDescription());
				logger.info("----Error message:"+e.getApiError().getMessage());
			   
				String status="Failed";	
		    	
		    	Double ramount=totalamount.doubleValue();
				Double famount=ramount/100;
		    	
		    	String chars = "0123456789";
			    int string_length = 5;
				String randomstring = "";
				for (int i=0; i<string_length; i++) {
					int rnum = (int) Math.floor(Math.random() * chars.length());
					randomstring += chars.substring(rnum,rnum+1);	
				}	
				
				    JsonObject errorJson=new JsonObject();
				    
				    errorJson.addProperty("status", status);
				    errorJson.addProperty("locale", locale.toString());
				    errorJson.addProperty("source", ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
				    errorJson.addProperty("total_amount", famount);
				    errorJson.addProperty("error", e.getApiError().getDescription());
				    errorJson.addProperty("otherData", e.getApiError().getMessage());
				    errorJson.addProperty("transactionId", randomstring+"-"+clientId);
				    errorJson.addProperty("currency", CurrencyCode.GBP.toString());
				    errorJson.addProperty("clientId", clientId);
				    logger.info("-----Error Json sendi to UI-----"+errorJson.toString());
				    
				    String result= onlinePaymentGateway(errorJson.toString());
				    
			    logger.info("-----Error Json sendi to UI-----"+errorJson.toString()+" Result is "+result.toString());
			    
			    recurringBillingHistory.setClientId(clientId);
			    recurringBillingHistory.setSource(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
			    recurringBillingHistory.setTransactionStatus(status); //status
			    recurringBillingHistory.setObsStatus(status); //status
				//recurringBillingHistory.setTransactionId(randomstring+"-"+clientId);
				recurringBillingHistory.setTransactionId(randomstring+"-"+clientId);
				recurringBillingHistory.setTransactionDate(DateUtils.getDateOfTenant());
				recurringBillingHistory.setTransactionCategory(ConfigurationConstants.WORLDPAY_PAYMENTGATEWAY_RECURRING);
			    recurringBillingHistory.setObsDescription(e.getApiError().getDescription());
			    recurringBillingHistory.setOrderId(null);
			    recurringBillingHistory.setTransactionData(e.getApiError().getMessage());
			    this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			    
		       logger.info("------------Error-----Updated in Recurring Histroy----------------"+clientId+"----------------");
			   
			    return new CommandProcessingResult(e.getApiError().getMessage(), clientId);
			}
			finally{
				 
				return new CommandProcessingResult(clientId, clientId);
			}
			
			
	 }

	
}
