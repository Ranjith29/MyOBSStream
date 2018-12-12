/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.api.contract.v1.ARBGetSubscriptionRequest;
import net.authorize.api.contract.v1.ARBGetSubscriptionResponse;
import net.authorize.api.contract.v1.ARBSubscriptionMaskedType;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.controller.ARBGetSubscriptionController;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.arb.Result;
import net.authorize.arb.Transaction;
import net.authorize.arb.TransactionType;
import net.authorize.data.arb.PaymentSchedule;
import net.authorize.data.arb.Subscription;
import net.authorize.data.arb.SubscriptionStatusType;
import net.authorize.data.arb.SubscriptionUnitType;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccountType;
import net.authorize.data.xml.Address;
import net.authorize.data.xml.BankAccount;
import net.authorize.data.xml.Customer;
import net.authorize.data.xml.Payment;
import net.authorize.xml.Message;

import org.joda.time.LocalDate;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistoryRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.recurring.exception.RecurringBillingNotFoundException;
import org.obsplatform.finance.paymentsgateway.recurring.exception.RecurringProfileNotMatchingException;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author ashokreddy
 *
 */
@Service
public class AuthorizeRecurringBillingProfileProcessImpl implements AuthorizeRecurringBillingProfileProcess {

	private final FromJsonHelper fromJsonHelper;
	private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;
	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private String currency;
	private static final String formatter = "yyyy-MM-dd";
	private static final String code = "1";

	@Autowired
	public AuthorizeRecurringBillingProfileProcessImpl(final FromJsonHelper fromJsonHelper,
			final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper,
			final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final RecurringBillingHistoryRepository recurringBillingHistoryRepository,
			final ClientRepositoryWrapper clientRepositoryWrapper) {

		this.fromJsonHelper = fromJsonHelper;
		this.paymentGatewayConfigurationRepositoryWrapper = paymentGatewayConfigurationRepositoryWrapper;
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
		this.recurringBillingHistoryRepository = recurringBillingHistoryRepository;
		this.clientRepositoryWrapper = clientRepositoryWrapper;
	}

	/**
	 * Used this Method to Create the Merchant Object with Extracting the
	 * Authorize Configuration data from PaymentGatewayConfiguration table.
	 * 
	 * @return {@link Merchant}
	 */
	public Merchant getMerchant() {

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

	/**
	 * We can Use this Method to create Authorize.net ARB Profile.
	 * 
	 * @param jsonRequestBody
	 * 
	 * @return response of the Authorize.net PaymentGateway.
	 */
	@Override
	public String createARBProfile(final String jsonRequestBody) {
		return createAuthorizeARBProfile(jsonRequestBody);
	}

	/**
	 * We can use this method to cancel the ARB Profile.
	 * 
	 * Get the status of profile by using ARB subscriberId. If it is In Active
	 * Mode, then We cancel the Profile.
	 * 
	 * @param jsonRequestBody
	 * @return
	 */
	@Override
	public String cancelARBProfile(final String jsonRequestBody) {
		return cancelAuthorizeARBProfile(jsonRequestBody);
	}
	
	/**
	 * We can use this method to update the Authorize.net ARB Profile.
	 * 
	 * Get the status of profile by using ARB subscriberId. If it is In Active
	 * Mode, then We cancel the Profile.
	 * 
	 * @param jsonRequestBody
	 * @return
	 */
	@Override
	public String updateARBProfile(final String jsonRequestBody) {
		return updateAuthorizeARBProfile(jsonRequestBody);
	}

	private String updateAuthorizeARBProfile(String jsonRequestBody) {
		
		final JsonElement commandElement = this.fromJsonHelper.parse(jsonRequestBody);
		
		final Long orderId = this.fromJsonHelper.extractLongNamed("orderId", commandElement);

		final RecurringBilling recurringBilling = validateOrderId(orderId);
		
		final String subscriberId = recurringBilling.getSubscriberId();
		final Subscription subscription = Subscription.createSubscription();
		subscription.setSubscriptionId(subscriberId);
		
		final Merchant merchant = getMerchant();
		final TransactionType transactionType = TransactionType.UPDATE_SUBSCRIPTION;

		final String paymentType = this.fromJsonHelper.extractStringNamed("paymentType", commandElement);
		final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", commandElement);
		final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

		Customer customer = Customer.createCustomer();
		Address bill_to = Address.createAddress();
		bill_to.setFirstName(client.getFirstname());
		bill_to.setLastName(client.getLastname());
		customer.setBillTo(bill_to);

		if (null != client.getEmail()) {
			customer.setEmail(client.getEmail());
		}

		if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_CREDIT_CARD_TYPE)) {
			final String cardNumber = this.fromJsonHelper.extractStringNamed("cardNumber", commandElement);
			final String month = this.fromJsonHelper.extractStringNamed("month", commandElement);
			final String year = this.fromJsonHelper.extractStringNamed("year", commandElement);
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setCreditCardNumber(cardNumber);
			creditCard.setExpirationMonth(month);
			creditCard.setExpirationYear(year);
			Payment payment = Payment.createPayment(creditCard);
			subscription.setPayment(payment);

		} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_BANK_ACCOUNT)) {
			final String bankName = this.fromJsonHelper.extractStringNamed("bankName", commandElement);
			final String accountNumber = this.fromJsonHelper.extractStringNamed("accountNumber", commandElement);
			final String routingNumber = this.fromJsonHelper.extractStringNamed("routingNumber", commandElement);
			final String accountName = this.fromJsonHelper.extractStringNamed("accountName", commandElement);
			final String bankAccountType = this.fromJsonHelper.extractStringNamed("bankAccountType", commandElement);

			final BankAccountType accountType = BankAccountType.findByValue(bankAccountType);

			BankAccount account = BankAccount.createBankAccount();

			account.setBankAccountName(accountName);
			account.setBankAccountNumber(accountNumber);
			account.setBankAccountType(accountType);
			account.setBankName(bankName);
			account.setRoutingNumber(routingNumber);
			Payment payment = Payment.createPayment(account);
			subscription.setPayment(payment);
		
		} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_RECURRING_AMOUNT)) {
			final BigDecimal amount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("total_amount", commandElement);
			subscription.setAmount(amount);
		}

		final Transaction arbTransaction = Transaction.createTransaction(merchant, transactionType, subscription);

		final Result<?> arbResult = (Result<?>) merchant.postTransaction(arbTransaction);

		return processARBOutput(arbResult, clientId, null, transactionType.getValue(),
				RecurringPaymentTransactionTypeConstants.UPDATE);
	
	}

	/**
	 * We can use this method to get the ARB Profile.
	 *
	 * @param jsonRequestBody
	 * @return
	 */
	public String getARBProfile(final String jsonRequestBody) {
		return getAuthorizeARBProfile(jsonRequestBody);
	}

	/**
	 * Use this Method to get the Status of a Recurring Profile. 1) we are
	 * calling {@link #getAuthorizeARBProfile(String)} method to get the Details
	 * of the Authorize Profile.
	 * 
	 * 2) Later we can extract the Status from the Profile Details.
	 * 
	 * @return {@link SubscriptionStatusType} value.
	 */
	@SuppressWarnings("unused")
	private SubscriptionStatusType getProfileStatus(final String jsonRequestBody) {

		final String jsonOutput = getAuthorizeARBProfile(jsonRequestBody);
		final JsonElement element = this.fromJsonHelper.parse(jsonOutput);
		final String status = this.fromJsonHelper.extractStringNamed("profileStatus", element);

		if (null == status)
			return null;
		return SubscriptionStatusType.fromValue(status);
	}

	private String getAuthorizeARBProfile(final String jsonRequestBody) {

		final JsonElement element = this.fromJsonHelper.parse(jsonRequestBody);
		final Long orderId = this.fromJsonHelper.extractLongNamed("orderId", element);
		final RecurringBilling recurringBilling = validateOrderId(orderId);
		
		final Merchant merchant = getMerchant();
		final TransactionType transactionType = TransactionType.GET_SUBSCRIPTION_STATUS;
		final Subscription subscription = Subscription.createSubscription();
		final String subscriberId = recurringBilling.getSubscriberId();
		subscription.setSubscriptionId(subscriberId);
		ApiOperationBase.setEnvironment(merchant.getEnvironment());
		MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType() ;
		merchantAuthenticationType.setName(merchant.getLogin());
		merchantAuthenticationType.setTransactionKey(merchant.getTransactionKey());
		 ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);
	        // Making the API request
	        ARBGetSubscriptionRequest apiRequest = new ARBGetSubscriptionRequest();
	        apiRequest.setRefId("Sample");
	        apiRequest.setSubscriptionId(this.fromJsonHelper.extractStringNamed("subscriberId", element));
	        // Calling the controller
	        ARBGetSubscriptionController controller = new ARBGetSubscriptionController(apiRequest);
	        controller.execute();
	        // Getting the response
	        ARBGetSubscriptionResponse response = controller.getApiResponse();
	       ARBSubscriptionMaskedType subscriptionResponse = response.getSubscription();
	       String amount = subscriptionResponse.getAmount().toString();

		/*final Transaction arbTransaction = Transaction.createTransaction(merchant, transactionType, subscription);
		final Result<?> arbResult = (Result<?>) merchant.postTransaction(arbTransaction);
*/
		return amount;
	}

	private String cancelAuthorizeARBProfile(String jsonRequestBody) {

		final JsonElement element = this.fromJsonHelper.parse(jsonRequestBody);
		RecurringBilling recurringBilling = null;
		
		if(this.fromJsonHelper.parameterExists("orderId", element)) {		
			final Long orderId = this.fromJsonHelper.extractLongNamed("orderId", element);
			recurringBilling = validateOrderId(orderId);
		
		} else if (this.fromJsonHelper.parameterExists("subscriberId", element)) {		
			final String subscriberId = this.fromJsonHelper.extractStringNamed("subscriberId", element);
			recurringBilling = validateSubscriberId(subscriberId);		
		} else {			
			System.out.println("orderId (or) subscriberId value must not be null");
			throw new RecurringBillingNotFoundException(-1L);
		}
		
		final Merchant merchant = getMerchant();
		final TransactionType transactionType = TransactionType.CANCEL_SUBSCRIPTION;

		final Subscription subscription = Subscription.createSubscription();
		final String subscriberId = recurringBilling.getSubscriberId();

		subscription.setSubscriptionId(subscriberId);

		final Transaction arbTransaction = Transaction.createTransaction(merchant, transactionType, subscription);

		final Result<?> arbResult = (Result<?>) merchant.postTransaction(arbTransaction);

		return processARBOutput(arbResult, recurringBilling.getClientId(), subscriberId, transactionType.getValue(),
				RecurringPaymentTransactionTypeConstants.CANCEL);

	}

	@SuppressWarnings("static-access")
	private String createAuthorizeARBProfile(final String jsonRequestBody) {

		final Merchant merchant = getMerchant();

		final JsonElement commandElement = this.fromJsonHelper.parse(jsonRequestBody);
		final TransactionType transactionType = TransactionType.CREATE_SUBSCRIPTION;

		final String payTermCode = this.fromJsonHelper.extractStringNamed("payTermCode", commandElement);
		final BigDecimal amount = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("total_amount", commandElement);
		final String paymentType = this.fromJsonHelper.extractStringNamed("paymentType", commandElement);
		final String recurringName = this.fromJsonHelper.extractStringNamed("recurringName", commandElement);
		final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", commandElement);
		final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

		Customer customer = Customer.createCustomer();
		Address bill_to = Address.createAddress();
		bill_to.setFirstName(client.getFirstname());
		bill_to.setLastName(client.getLastname());
		customer.setBillTo(bill_to);

		if (null != client.getEmail()) {
			customer.setEmail(client.getEmail());
		}

		LocalDate localDate = new DateUtils().getLocalDateOfTenant();
		final String date = localDate.toString(formatter);

		PaymentSchedule new_schedule = getPaymentScheduleData(payTermCode);
		new_schedule.setStartDate(date);
		new_schedule.setTotalOccurrences(9999);
		new_schedule.setTrialOccurrences(0);

		Subscription subscription = Subscription.createSubscription();
		subscription.setAmount(amount);
		subscription.setName(recurringName == null ? "Recurring" : recurringName);
		subscription.setSchedule(new_schedule);
		subscription.setCustomer(customer);

		if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_CREDIT_CARD_TYPE)) {
			final String cardNumber = this.fromJsonHelper.extractStringNamed("cardNumber", commandElement);
			final String month = this.fromJsonHelper.extractStringNamed("month", commandElement);
			final String year = this.fromJsonHelper.extractStringNamed("year", commandElement);
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setCreditCardNumber(cardNumber);
			creditCard.setExpirationMonth(month);
			creditCard.setExpirationYear(year);
			Payment payment = Payment.createPayment(creditCard);
			subscription.setPayment(payment);

		} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_BANK_ACCOUNT)) {

			final String bankName = this.fromJsonHelper.extractStringNamed("bankName", commandElement);
			final String accountNumber = this.fromJsonHelper.extractStringNamed("accountNumber", commandElement);
			final String routingNumber = this.fromJsonHelper.extractStringNamed("routingNumber", commandElement);
			final String accountName = this.fromJsonHelper.extractStringNamed("accountName", commandElement);
			final String bankAccountType = this.fromJsonHelper.extractStringNamed("bankAccountType", commandElement);

			final BankAccountType accountType = BankAccountType.findByValue(bankAccountType);

			BankAccount account = BankAccount.createBankAccount();

			account.setBankAccountName(accountName);
			account.setBankAccountNumber(accountNumber);
			account.setBankAccountType(accountType);
			account.setBankName(bankName);
			account.setRoutingNumber(routingNumber);
			Payment payment = Payment.createPayment(account);
			subscription.setPayment(payment);
		}

		final Transaction arbTransaction = Transaction.createTransaction(merchant, transactionType, subscription);

		final Result<?> arbResult = (Result<?>) merchant.postTransaction(arbTransaction);

		return processARBOutput(arbResult, clientId, null, transactionType.getValue(),
				RecurringPaymentTransactionTypeConstants.CREATE);
	}

	private PaymentSchedule getPaymentScheduleData(final String payTermCode) {

		PaymentSchedule new_schedule = PaymentSchedule.createPaymentSchedule();

		SubscriptionUnitType subscription_unit;

		switch (payTermCode) {

		case ConfigurationConstants.PAYTERM_WEEKLY:
			new_schedule.setIntervalLength(7);
			subscription_unit = SubscriptionUnitType.DAYS;
			new_schedule.setSubscriptionUnit(subscription_unit);
			break;

		case ConfigurationConstants.PAYTERM_MONTHLY:
			new_schedule.setIntervalLength(1);
			subscription_unit = SubscriptionUnitType.MONTHS;
			new_schedule.setSubscriptionUnit(subscription_unit);
			break;

		case ConfigurationConstants.PAYTERM_QUATERLY:
			new_schedule.setIntervalLength(3);
			subscription_unit = SubscriptionUnitType.MONTHS;
			new_schedule.setSubscriptionUnit(subscription_unit);
			break;

		case ConfigurationConstants.PAYTERM_HALFYEARLY:
			new_schedule.setIntervalLength(6);
			subscription_unit = SubscriptionUnitType.MONTHS;
			new_schedule.setSubscriptionUnit(subscription_unit);
			break;

		case ConfigurationConstants.PAYTERM_YEARLY:
			new_schedule.setIntervalLength(12);
			subscription_unit = SubscriptionUnitType.MONTHS;
			new_schedule.setSubscriptionUnit(subscription_unit);
			break;

		default:
			break;
		}
		return new_schedule;
	}

	private RecurringBilling validateOrderId(final Long orderId) {

		final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneByOrderIdNotNull(orderId);

		if (!recurringBilling.getGatewayName().equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET)) {
			throw new RecurringProfileNotMatchingException(orderId,
					RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET);
		}
		return recurringBilling;
	}
	
	private RecurringBilling validateSubscriberId(final String subscriberId) {
		
		final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneBySubscriberIdNotNull(subscriberId);

		if (!recurringBilling.getGatewayName().equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET)) {
			throw new RecurringProfileNotMatchingException(subscriberId,
					RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET);
		}
		return recurringBilling;
	}

	private String processARBOutput(final Result<?> arbResult, final Long clientId, final String subscriberId,
			final String transactionCategory, final String action) {

		final JsonArray jsonArray = new JsonArray();
		final JsonObject jsonObject = new JsonObject();
		final RecurringBillingHistory recurringBillingHistory = RecurringBillingHistory.createHistory(clientId,
				transactionCategory, ConfigurationConstants.AUTHORIZENET_PAYMENTGATEWAY,
				DateUtils.getLocalDateOfTenant().toDate(), arbResult.getResultCode());

		final String resultCode = arbResult.getResultCode();
		final SubscriptionStatusType status = arbResult.getSubscriptionStatus();

		final ArrayList<Message> error = arbResult.getMessages();

		for (Message message : error) {
			JsonObject object = new JsonObject();
			object.addProperty("errorCode", message.getCode());
			object.addProperty("errorText", message.getText());
			jsonArray.add(object);
		}

		jsonObject.add("response", jsonArray);
		jsonObject.addProperty("clientId", clientId);
		jsonObject.addProperty("resultCode", resultCode);

		if (null != status) {
			jsonObject.addProperty("profileStatus", status.value());
		}

		if (arbResult.isOk()) {

			jsonObject.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
			recurringBillingHistory.setObsStatus(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS);
			recurringBillingHistory.setObsDescription("Recurring Profile " + action + " Successfully");

			

			if (action.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.CREATE)) {
				
				final RecurringBilling recurringBilling = new RecurringBilling(clientId, subscriberId,
						RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET);
				
				if(null == subscriberId) {
					final String subId = arbResult.getResultSubscriptionId();
					recurringBilling.setSubscriberId(subId);
					jsonObject.addProperty("subscriberId", subId);
					this.recurringBillingRepositoryWrapper.save(recurringBilling);
				}			
			}

		} else {

			jsonObject.addProperty("result", ConfigurationConstants.PAYMENTGATEWAY_FAILURE);

			recurringBillingHistory.setObsStatus(ConfigurationConstants.PAYMENTGATEWAY_FAILURE);
			recurringBillingHistory.setObsDescription("Recurring Profile " + action + " Failed");
		}

		recurringBillingHistory.setTransactionData(jsonObject.toString());
		this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		return jsonObject.toString();
	}

	/* (non-Javadoc)
	 * @see org.obsplatform.finance.paymentsgateway.recurring.service.AuthorizeRecurringBillingProfileProcess#notifyAuthorizeRequest(java.lang.String)
	 */
	@Override
	public String notifyAuthorizeRequest(final String jsonData) {
		
		final JsonElement element = this.fromJsonHelper.parse(jsonData);
		
		final String responseCode = this.fromJsonHelper.extractStringNamed("x_response_code", element);	
		final String responseSubcode = this.fromJsonHelper.extractStringNamed("x_response_subcode", element);		
		final String responseReasonCode = this.fromJsonHelper.extractStringNamed("x_response_reason_code", element);		
		final String transactionId = this.fromJsonHelper.extractStringNamed("x_trans_id", element);	
			
		if(this.fromJsonHelper.parameterExists("x_subscription_id", element)) {
			
			final String subscriptionId = this.fromJsonHelper.extractStringNamed("x_subscription_id", element);
			
			final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneBySubscriberId(subscriptionId);
			
			if(null == recurringBilling) {
				// develop error Message and sent to OBS Admistrator and return error
			}
			final String responseReasonText = this.fromJsonHelper.extractStringNamed("x_response_reason_text", element);
			
			if(code.equals(responseCode) && code.equals(responseSubcode) && code.equals(responseReasonCode)) {
				
				final String status = RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS;
				
				final RecurringBillingHistory history = RecurringBillingHistory.createHistory(recurringBilling.getClientId(), 
						RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT, 
						RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET, 
						DateUtils.getLocalDateOfTenant().toDate(), status);
				
				if(null != recurringBilling.getOrderId()) {
					history.setOrderId(recurringBilling.getOrderId());			
				}
				
				history.setTransactionData(jsonData);
				history.setObsStatus(status);
				history.setObsDescription("responseReasonText from Authorize Notify Request : " + responseReasonText);	
				this.recurringBillingHistoryRepository.save(history);		
				
				if(this.fromJsonHelper.parameterExists("x_subscription_paynum", element)) {
					
					final BigDecimal amount = BigDecimal.valueOf(Double.valueOf(this.fromJsonHelper.extractStringNamed("x_amount", element)));
					final String paymentNo = this.fromJsonHelper.extractStringNamed("x_subscription_paynum", element);
					
					JsonObject jsonObject = new JsonObject();
					
					String uniqueTransaction = transactionId + "-" + subscriptionId + "-" + paymentNo;
					jsonObject.addProperty("source", RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET);
					jsonObject.addProperty("transactionId", uniqueTransaction);
					jsonObject.addProperty("currency", currency == null ? "USD" : currency);
					jsonObject.addProperty("clientId", recurringBilling.getClientId());
					jsonObject.addProperty("total_amount", amount);
					jsonObject.addProperty("locale", "en");
					jsonObject.addProperty("dateFormat", "dd MMMM yyyy");
					jsonObject.addProperty("otherData", jsonData);
					jsonObject.addProperty("status", RecurringPaymentTransactionTypeConstants.SUCCESS);
					jsonObject.addProperty("recurringHistoryId", history.getId());
					
					return jsonObject.toString();				
				} 
			} else {	
				
				final String status = RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE;
				RecurringBillingHistory history = RecurringBillingHistory.createHistory(recurringBilling.getClientId(), 
						RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT, 
						RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_AUTHORIZENET, 
						DateUtils.getLocalDateOfTenant().toDate(), status);
				
				if(null != recurringBilling.getOrderId()) {
					history.setOrderId(recurringBilling.getOrderId());			
				}
				
				history.setTransactionData(jsonData);
				history.setObsStatus(status);
				history.setObsDescription("responseReasonText from Authorize Notify Request : " + responseReasonText);
				
				this.recurringBillingHistoryRepository.save(history);		
			}
			
		}
		return null;
	}
}
