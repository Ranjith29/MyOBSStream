package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.authorize.Merchant;
import net.authorize.ResponseCode;
import net.authorize.aim.Result;
import net.authorize.TransactionType;
import net.authorize.aim.Transaction;
import net.authorize.data.creditcard.CreditCard;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistoryRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.recurring.serialization.RecuuringCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class PaymentGatewayRecurringWritePlatformServiceImpl implements PaymentGatewayRecurringWritePlatformService {

	private final Logger logger = LoggerFactory.getLogger(PaymentGatewayRecurringWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;
	private final RecuuringCommandFromApiJsonDeserializer recuuringCommandFromApiJsonDeserializer;
	private final AuthorizeRecurringBillingProfileProcess authorizeRecurringBillingProfileProcess;
	private final PaypalRecurringBillingProfileProcess paypalRecurringBillingProfileProcess;
	private final AuthorizeRecurringBillingProfileProcessImpl authorizeRecurringBillingProfileProcessimpl;
	private final FromJsonHelper fromJsonHelper;

	@Autowired
	public PaymentGatewayRecurringWritePlatformServiceImpl(final PlatformSecurityContext context,
			final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final RecurringBillingHistoryRepository recurringBillingHistoryRepository,
			final RecuuringCommandFromApiJsonDeserializer recuuringCommandFromApiJsonDeserializer,
			final AuthorizeRecurringBillingProfileProcess authorizeRecurringBillingProfileProcess,
			final PaypalRecurringBillingProfileProcess paypalRecurringBillingProfileProcess,
			final FromJsonHelper fromJsonHelper,final AuthorizeRecurringBillingProfileProcessImpl authorizeRecurringBillingProfileProcessimpl) {
		this.context = context;
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
		this.recurringBillingHistoryRepository = recurringBillingHistoryRepository;
		this.recuuringCommandFromApiJsonDeserializer = recuuringCommandFromApiJsonDeserializer;
		this.authorizeRecurringBillingProfileProcess = authorizeRecurringBillingProfileProcess;
		this.paypalRecurringBillingProfileProcess = paypalRecurringBillingProfileProcess;
		this.fromJsonHelper = fromJsonHelper;
		this.authorizeRecurringBillingProfileProcessimpl=authorizeRecurringBillingProfileProcessimpl;
	}
	
	@Override
	public String paypalRecurringVerification(final HttpServletRequest request)
			throws IllegalStateException, ClientProtocolException, IOException, JSONException {
		return this.paypalRecurringBillingProfileProcess.recurringVerification(request);
	}

	@Override
	public void recurringEventUpdate(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) throws JSONException {
		this.paypalRecurringBillingProfileProcess.recurringProfileUpdate(request, recurringBillingHistory);
	}

	@Override
	public RecurringBilling recurringSubscriberSignUp(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) {
		return this.paypalRecurringBillingProfileProcess.recurringProfileCreation(request, recurringBillingHistory);
	}

	@Override
	public String createJsonForOnlineMethod(final HttpServletRequest request) throws JSONException {
		return this.paypalRecurringBillingProfileProcess.createJsonForOnlineMethod(request);
	}

	@Override
	public CommandProcessingResult updatePaypalRecurring(final JsonCommand command) {
		return this.paypalRecurringBillingProfileProcess.updatePaypalRecurring(command);
	}

	@Override
	public CommandProcessingResult updatePaypalProfileStatus(final JsonCommand command) {
		return this.paypalRecurringBillingProfileProcess.updatePaypalProfileStatus(command);
	}

	@Override
	public void disConnectOrder(final String profileId, final RecurringBillingHistory recurringBillingHistory) {

		this.context.authenticatedUser();
		final Long orderId = getOrderIdFromSubscriptionId(profileId);

		if (null == orderId) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("order is Not with this ProfileId:" + profileId);
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			return;
		}

		this.paypalRecurringBillingProfileProcess.paypalDisConnectOrder(orderId, profileId, recurringBillingHistory);
	}

	private Long getOrderIdFromSubscriptionId(final String profileId) {

		final RecurringBilling billing = this.paypalRecurringBillingProfileProcess.getRecurringBillingObject(profileId);

		if (null == billing || null == billing.getOrderId()) {
			logger.info("orderId Not found with this SubscriberId:" + profileId);
			return null;
		}

		return billing.getOrderId();
	}

	@Override
	public Long updateRecurringBillingTable(final String profileId) {

		final RecurringBilling billing = this.paypalRecurringBillingProfileProcess.getRecurringBillingObject(profileId);
		if (null == billing) {
			logger.info("orderId Not found with this SubscriberId:" + profileId);
			return null;
		}

		billing.updateStatus();
		this.recurringBillingRepositoryWrapper.save(billing);
		return billing.getOrderId();
	}

	@Override
	public CommandProcessingResult deleteRecurringBilling(JsonCommand command) {

		this.context.authenticatedUser();
		final String subscriptionId = command.stringValueOfParameterNamed(RecurringPaymentTransactionTypeConstants.SUBSCRID);
		final CommandProcessingResult result = updatePaypalProfileStatus(command);

		if (null != result) {
			
			final Map<String, Object> resultmap = result.getChanges();
			final String outputRes = resultmap.get("result").toString();

			if (outputRes.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS)) {
				final Long orderId = updateRecurringBillingTable(subscriptionId);
				return new CommandProcessingResult(orderId);
			} else {
				return new CommandProcessingResult(0L);
			}
		} else {
			return new CommandProcessingResult(new Long(-1));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getRequestParameters(final HttpServletRequest request) {

		final Enumeration<String> enumeration = request.getParameterNames();
		JsonObject object = new JsonObject();
		while (enumeration.hasMoreElements()) {
			final String paramName = enumeration.nextElement();
			final String paramValue = request.getParameter(paramName);
			if (!"password".equalsIgnoreCase(paramName))
				object.addProperty(paramName, paramValue);
		}
		return object.toString();
	}

	@Override
	public CommandProcessingResult processAuthorizeRecurringBillingProfile(final JsonCommand command) {

		this.recuuringCommandFromApiJsonDeserializer.validateForProfileProcess(command.json());
		final String actionName = command.stringValueOfParameterNamed("actionName");
		final Long clientId = command.longValueOfParameterNamed("clientId");
		
		this.recuuringCommandFromApiJsonDeserializer.validateForAuthorizeRecurringProfile(command.json());
		
		if (actionName.equalsIgnoreCase(ConfigurationConstants.CREATE) ) {		
			
			String output = this.authorizeRecurringBillingProfileProcess.createARBProfile(command.json());
			
			JsonElement element= this.fromJsonHelper.parse(output);
			String result = this.fromJsonHelper.extractStringNamed("result", element);
			
			if(result.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS) && command.longValueOfParameterNamed("depositamount")!=0) {
				ResponseCode depositResult = Initialpayment(command.json());
				JsonObject jsonobject=element.getAsJsonObject();
				jsonobject.addProperty("depositResult", depositResult.toString());
				output = jsonobject.toString();
			}else{
				JsonObject jsonobject=element.getAsJsonObject();
				jsonobject.addProperty("depositResult", "APPROVED");
				output = jsonobject.toString();
			}
			return new CommandProcessingResult(output,clientId);
		} else if (actionName.equalsIgnoreCase(ConfigurationConstants.CANCEL)) {
			final String output = this.authorizeRecurringBillingProfileProcess.cancelARBProfile(command.json());
			return new CommandProcessingResult(output, clientId);
		} else if (actionName.equalsIgnoreCase(ConfigurationConstants.UPDATE)) {
			final String output = this.authorizeRecurringBillingProfileProcess.updateARBProfile(command.json());
			return new CommandProcessingResult(output, clientId);
		}else if (actionName.equalsIgnoreCase(ConfigurationConstants.GET)) {
			final String output = this.authorizeRecurringBillingProfileProcess.getARBProfile(command.json());
			return new CommandProcessingResult(output, clientId);
		}
		return new CommandProcessingResult(clientId);
	}

	private ResponseCode Initialpayment(String jsonRequestBody) {
		
		ResponseCode Result = null;
		Merchant merchant = this.authorizeRecurringBillingProfileProcessimpl.getMerchant();
		final JsonElement commandElement = this.fromJsonHelper.parse(jsonRequestBody);
		CreditCard creditCard = CreditCard.createCreditCard();
		final String cardNumber = this.fromJsonHelper.extractStringNamed("cardNumber", commandElement);
		final String month = this.fromJsonHelper.extractStringNamed("month", commandElement);
		final String year = this.fromJsonHelper.extractStringNamed("year", commandElement);
		creditCard.setCreditCardNumber(cardNumber);
		creditCard.setExpirationMonth(month);
		creditCard.setExpirationYear(year);
		final String depositamount = this.fromJsonHelper.extractStringNamed("depositamount", commandElement);
		Transaction authCaptureTransaction = merchant.createAIMTransaction(TransactionType.AUTH_CAPTURE, new BigDecimal(depositamount));
		authCaptureTransaction.setCreditCard(creditCard);
		//Result<?> result = merchant.postTransaction(authCaptureTransaction);
		Result<?> result = (Result<?>)merchant.postTransaction(authCaptureTransaction);
		return Result = result.getResponseCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.obsplatform.finance.paymentsgateway.recurring.service.
	 * PaymentGatewayRecurringWritePlatformService
	 * #updateRecurringBillingProfile(org.obsplatform.infrastructure.core.api.
	 * JsonCommand)
	 */
	/**
	 * We can use this Service method to update the orderId in b_recurring table
	 * and update the history in b_recurring_history table.
	 */
	@Override
	public CommandProcessingResult updateRecurringBillingProfileOrderId(final JsonCommand command) {
		this.recuuringCommandFromApiJsonDeserializer.validateForUpdateRecurringBillingProfile(command.json());
		final Long orderId = command.longValueOfParameterNamed("orderId");
		final Long clientId = command.longValueOfParameterNamed("clientId");
		final String subscriberId = command.stringValueOfParameterNamed("subscriberId");
		final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findRecurringProfile(clientId, subscriberId);
		recurringBilling.setOrderId(orderId);
		this.recurringBillingRepositoryWrapper.save(recurringBilling);
		return new CommandProcessingResultBuilder().withCommandId(recurringBilling.getOrderId()).withEntityId(recurringBilling.getId()).build();
	}

	@Override
	public String notifyAuthorizeRecurringBillingRequest(String jsonCommand) {	
		this.recuuringCommandFromApiJsonDeserializer.validateForNotifyAuthorizeRequest(jsonCommand);
		return this.authorizeRecurringBillingProfileProcess.notifyAuthorizeRequest(jsonCommand);
	}

	@Override
	public CommandProcessingResult createRecurringBilling(JsonCommand command) {
		
		this.context.authenticatedUser();
		this.recuuringCommandFromApiJsonDeserializer.validateForCreate(command.json());
		
		final Long clientId = command.longValueOfParameterNamed("clientId");
		final Long orderId = command.longValueOfParameterNamed("orderId");
		final String gatewayName = command.stringValueOfParameterNamed("gatewayName");
		
		final RecurringBilling recurringBilling = new RecurringBilling(clientId, clientId.toString(), gatewayName);
		recurringBilling.setOrderId(orderId);
		
		this.recurringBillingRepositoryWrapper.save(recurringBilling);
		return new CommandProcessingResultBuilder().withCommandId(recurringBilling.getOrderId()).withEntityId(recurringBilling.getId()).build();
	}
	

}
