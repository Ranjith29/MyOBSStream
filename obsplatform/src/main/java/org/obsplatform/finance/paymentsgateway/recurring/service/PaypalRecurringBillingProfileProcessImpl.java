/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.finance.paymentsgateway.exception.PaypalStatusChangeActionTypeMisMatchException;
import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringPaymentTransactionTypeConstants;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistoryRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.exceptions.OrderNotFoundException;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventaction.service.EventActionReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;

import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusReq;
import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusRequestType;
import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileReq;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileRequestType;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.AckCodeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.ManageRecurringPaymentsProfileStatusRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.StatusChangeActionType;
import urn.ebay.apis.eBLBaseComponents.UpdateRecurringPaymentsProfileRequestDetailsType;

/**
 * @author ashokreddy
 *
 */
@SuppressWarnings("deprecation")
@Service
public class PaypalRecurringBillingProfileProcessImpl implements PaypalRecurringBillingProfileProcess {

	private final Logger logger = LoggerFactory.getLogger(PaypalRecurringBillingProfileProcessImpl.class);
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromJsonHelper;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final EventActionReadPlatformService eventActionReadPlatformService;
	private final EventActionRepository eventActionRepository;
	private final OrderRepository orderRepository;
	private final ConfigurationRepository configurationRepository;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;

	private final HttpClient client = new DefaultHttpClient();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");

	@Autowired
	public PaypalRecurringBillingProfileProcessImpl(final PlatformSecurityContext context,
			final FromJsonHelper fromJsonHelper,
			final PortfolioCommandSourceWritePlatformService writePlatformService,
			final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper,
			final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final EventActionRepository eventActionRepository, final OrderRepository orderRepository,
			final ConfigurationRepository configurationRepository,
			final RecurringBillingHistoryRepository recurringBillingHistoryRepository) {

		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.writePlatformService = writePlatformService;
		this.paymentGatewayConfigurationRepositoryWrapper = paymentGatewayConfigurationRepositoryWrapper;
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
		this.eventActionReadPlatformService = eventActionReadPlatformService;
		this.eventActionRepository = eventActionRepository;
		this.orderRepository = orderRepository;
		this.configurationRepository = configurationRepository;
		this.recurringBillingHistoryRepository = recurringBillingHistoryRepository;
	}

	/**
	 * Paypal Recurring Verification Method is Used for Verifying the IPN
	 * Handler Request. Which is Came from Paypal Server For The
	 * Changes/Creation of Paypal Recurring Profile.
	 * 
	 * For this Verification, We have to re-send the Response Parameter to
	 * Paypal Server.
	 * 
	 * NOTE: "password","username" and "rm" Parameters are Explicity add to
	 * Paypal Response Parameters, So We have to Delete that Parameters From
	 * Verification Sending Paypal Request. Otherwise We get INVALID Response
	 * From Paypal Server.
	 * 
	 * PROCESS: Prepare a String Variable with Response Parameter in the Form of
	 * Url-Encode. and Add the "requestData=_notify-validate"(Said to Paypal
	 * Server as it is a Verification Request) Content to Before String
	 * Variable.
	 * 
	 * And Send this String to Paypal Server(We can take Paypal Url From
	 * c_paymentgateway_conf table )
	 */

	@SuppressWarnings("unchecked")
	@Override
	public String recurringVerification(final HttpServletRequest request)
			throws IllegalStateException, ClientProtocolException, IOException, JSONException {

		BufferedReader bufferedReader = null;
		InputStream is = null;
		final String splitParam = "\\?", charSet = "charset";

		try {
			this.context.authenticatedUser();
			final String value = this.paymentGatewayConfigurationRepositoryWrapper.getValue(ConfigurationConstants.PAYPAL_PAYMENTGATEWAY);
			final JsonElement element = this.fromJsonHelper.parse(value);
			
			final String paypalUrl = this.fromJsonHelper.extractStringNamed(ConfigurationConstants.PAYPAL_URL_NAME, element);
			
			HttpPost post = new HttpPost(paypalUrl.split(splitParam)[0].trim());
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("cmd", "_notify-validate")); // You need to add this parameter to tell PayPal to verify.
			
			for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
				
				final String paramName = e.nextElement();
				final String paramValue = request.getParameter(paramName);

				if (!"password".equalsIgnoreCase(paramName) && !"username".equalsIgnoreCase(paramName)
						&& !"tenantIdentifier".equalsIgnoreCase(paramName) && !"rm".equalsIgnoreCase(paramName)) {
					params.add(new BasicNameValuePair(paramName, paramValue));
				}
			}
			post.setEntity(new UrlEncodedFormEntity(params, request.getParameter(charSet)));
			HttpResponse response = client.execute(post);

			is = response.getEntity().getContent();
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			String line, result = "";

			while ((line = bufferedReader.readLine()) != null) {
				result += line;
			}
			return result;

		} finally {
			if (null != is)
				is.close();
			if (null != bufferedReader)
				bufferedReader.close();
		}
	}

	/**
	 * This recurringEventUpdate(HttpServletRequest request) Method Used For
	 * Creating/Renewal/Change the Orders From EventAction Table.
	 * 
	 * @param recurringSubscriberSignUp(HttpServletRequest
	 *            request) is used for Storing ProfileId in b_recurring table if
	 *            Not stored.
	 * 
	 * @Param retrievePendingRecurringRequest(Long clientId) method is used for
	 *        get the Recurring(Status='R') Orders from b_event_action.
	 */

	public void recurringProfileUpdate(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) throws JSONException {

		this.context.authenticatedUser();
		final String ProfileId = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);		
		
		final RecurringBilling recurringBilling = getRecurringBillingObject(ProfileId);
		if (null == recurringBilling)
			return;

		final String jsonObj = customDataReturn(request);

		JSONObject obj = new JSONObject(jsonObj);

		final Long clientId = obj.getLong(RecurringPaymentTransactionTypeConstants.CLIENTID);
		final List<EventActionData> eventActionDatas = this.eventActionReadPlatformService.retrievePendingRecurringRequest(clientId);

		for (EventActionData eventActionData : eventActionDatas) {

			logger.info("EventAction Id: " + eventActionData.getId() + ", clientId: " + clientId + ", actionName: " + eventActionData.getActionName());
			recurringBillingHistory.setClientId(clientId);
			
			final EventAction eventAction = this.eventActionRepository.findOne(eventActionData.getId());

			if (eventAction.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_NEW)) {

				final Long planId = obj.getLong(RecurringPaymentTransactionTypeConstants.PLANID);
				final String paytermCode = obj.getString(RecurringPaymentTransactionTypeConstants.PAYTERMCODE);
				final Long contractPeriod = obj.getLong(RecurringPaymentTransactionTypeConstants.CONTRACTPERIOD);

				JSONObject createOrder = new JSONObject(eventAction.getCommandAsJson());

				final Long ePlanCode = createOrder.getLong(RecurringPaymentTransactionTypeConstants.PLANCODE);
				final String ePaytermCode = createOrder.getString(RecurringPaymentTransactionTypeConstants.PAYTERMCODE);
				final Long eContractPeriod = createOrder.getLong(RecurringPaymentTransactionTypeConstants.CONTRACTPERIOD);

				if (planId.equals(ePlanCode) && paytermCode.equalsIgnoreCase(ePaytermCode) && contractPeriod.equals(eContractPeriod)) {

					// creating order and assign Recurring Details.
					try {
						final CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(clientId).withJson(createOrder.toString()).build();
						final CommandProcessingResult resultOrder = this.writePlatformService.logCommandSource(commandRequest);

						if (null == resultOrder || resultOrder.resourceId() <= 0) {
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
							recurringBillingHistory.setObsDescription("OrderBooking Failed...");
							eventAction.updateStatus('F');
							eventAction.setDescription("orderBooking Failed with id=" + resultOrder.resourceId());
						}

						if (null != recurringBilling) {
							createOrder.remove("start_date");
							eventAction.updateStatus('Y');
							eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());
							createOrder.put("start_date", DateUtils.getLocalDateOfTenant().toDate());
							eventAction.setCommandAsJson(createOrder.toString());
							eventAction.setDescription("Success");
							recurringBilling.setOrderId(resultOrder.resourceId());
							this.recurringBillingRepositoryWrapper.save(recurringBilling);
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
							recurringBillingHistory.setObsDescription("OrderBooking Success...");
						}

					} catch (Exception e) {
						recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
						recurringBillingHistory.setObsDescription("OrderBooking Failed, Reason:" + stackTraceToString(e));
						eventAction.updateStatus('F');
						eventAction.setDescription("Reason:" + stackTraceToString(e));
					}
				}

			} else if (eventAction.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_CHNAGE_PLAN)) {

				// Do the Change Order Functionality Here
				JSONObject changeOrder = new JSONObject(eventAction.getCommandAsJson());
				
				final Long planId = obj.getLong(RecurringPaymentTransactionTypeConstants.PLANID);
				final String paytermCode = obj.getString(RecurringPaymentTransactionTypeConstants.PAYTERMCODE);
				final Long contractPeriod = obj.getLong(RecurringPaymentTransactionTypeConstants.CONTRACTPERIOD);
				final Long orderId = obj.getLong(RecurringPaymentTransactionTypeConstants.ORDERID);

				final Long ePlanCode = changeOrder.getLong(RecurringPaymentTransactionTypeConstants.PLANCODE);
				final String ePaytermCode = changeOrder.getString(RecurringPaymentTransactionTypeConstants.PAYTERMCODE);
				final Long eContractPeriod = changeOrder.getLong(RecurringPaymentTransactionTypeConstants.CONTRACTPERIOD);

				if (planId.equals(ePlanCode) && paytermCode.equalsIgnoreCase(ePaytermCode) && contractPeriod.equals(eContractPeriod)) {

					try {
						final CommandWrapper commandRequest = new CommandWrapperBuilder().changePlan(orderId).withJson(changeOrder.toString()).build();
						final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
						if (null == result || result.resourceId() <= 0) {
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
							recurringBillingHistory.setObsDescription("Change Plan Action Failed...");
							eventAction.updateStatus('F');
							eventAction.setDescription("result.resourceId=" + result.resourceId());
						}

						if (null != recurringBilling) {
							if (changeOrder.has("start_date")) {
								changeOrder.remove("start_date");
								changeOrder.put("start_date", DateUtils.getLocalDateOfTenant().toDate());
							}
							eventAction.updateStatus('Y');
							eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());
							eventAction.setCommandAsJson(changeOrder.toString());
							eventAction.setDescription("Success");
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
							recurringBillingHistory.setObsDescription("Change Plan Completed");
							recurringBilling.setOrderId(result.resourceId());
							this.recurringBillingRepositoryWrapper.save(recurringBilling);
						}

					} catch (Exception e) {
						recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
						recurringBillingHistory.setObsDescription("change Plan Action Failed, Reason:" + stackTraceToString(e));
						eventAction.updateStatus('F');
						eventAction.setDescription("StackTrace:" + stackTraceToString(e));
					}
				}

			} else if (eventAction.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_RENEWAL)) {

				JSONObject renewalOrder = new JSONObject(eventAction.getCommandAsJson());
				
				final Long renewalPeriod = obj.getLong(RecurringPaymentTransactionTypeConstants.RENEWALPERIOD);
				final Long priceId = obj.getLong(RecurringPaymentTransactionTypeConstants.PRICEID);
				final Long orderId = obj.getLong(RecurringPaymentTransactionTypeConstants.ORDERID);

				final Long eRenewalPeriod = renewalOrder.getLong(RecurringPaymentTransactionTypeConstants.RENEWALPERIOD);
				final Long ePriceId = renewalOrder.getLong(RecurringPaymentTransactionTypeConstants.PRICEID);
				final Long eOrderId = renewalOrder.getLong(RecurringPaymentTransactionTypeConstants.ORDERID);

				if (renewalOrder.has(RecurringPaymentTransactionTypeConstants.CLIENTID)) {
					renewalOrder.remove(RecurringPaymentTransactionTypeConstants.CLIENTID);
				}

				if (renewalOrder.has(RecurringPaymentTransactionTypeConstants.ORDERID)) {
					renewalOrder.remove(RecurringPaymentTransactionTypeConstants.ORDERID);
				}

				if (renewalPeriod.equals(eRenewalPeriod) && priceId.equals(ePriceId) && orderId.equals(eOrderId)) {

					try {
						final CommandWrapper commandRequest = new CommandWrapperBuilder().renewalOrder(orderId).withJson(renewalOrder.toString()).build();
						final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);

						if (null == result || result.resourceId() <= 0) {
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
							recurringBillingHistory.setObsDescription("Renewal Order Action Failed...");
							eventAction.updateStatus('F');
							eventAction.setDescription("result.resourceId=" + result.resourceId());
						}
						if (null != recurringBilling) {
							eventAction.updateStatus('Y');
							eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());
							eventAction.setDescription("Success");
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
							recurringBillingHistory.setObsDescription("Renewal Order Completed");
							recurringBilling.setOrderId(orderId);
							this.recurringBillingRepositoryWrapper.save(recurringBilling);
						}

					} catch (Exception e) {
						recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
						recurringBillingHistory.setObsDescription("Renewal Order Action Failed, Reason:" + stackTraceToString(e));
						eventAction.updateStatus('F');
						eventAction.setDescription("StackTrace:" + stackTraceToString(e));
					}
				}

			} else {
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				recurringBillingHistory.setObsDescription("Didn't Develop this Functionality");
				logger.info("Does Not Implement the Code....");
			}

			this.eventActionRepository.save(eventAction);
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		}
	}

	/**
	 * Use This Method to Storing Paypal Recurring Profile SubscriberId For
	 * Tracking the Recurring Profile. We can Use this SubscriberId to Identify
	 * the Recurring Profile in the Paypal Server.
	 * 
	 * Using this SubscriberId We can change the Recurring Profile Information.
	 * 
	 * @param clientId,subscriberId
	 *            is Storing in the table. One SubscriberId Belongs to one
	 *            Customer/ClientId only
	 * 
	 */
	public RecurringBilling recurringProfileCreation(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) {

		RecurringBilling billing;
		try {
			// look up from scheduleOrder and EventAction Table and add type parameter in json
			this.context.authenticatedUser();
			
			final String ProfileId = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);
			final String customData = customDataReturn(request);
			billing = this.recurringBillingRepositoryWrapper.findOneBySubscriberId(ProfileId);

			if (null == customData) {

				recurringBillingHistory.setClientId(0L);
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				recurringBillingHistory.setObsDescription("Profile Creation Checking Failed Due to custom Parameter Data Not Found in the Request");
				this.recurringBillingHistoryRepository.save(recurringBillingHistory);
				return null;
			}

			if (null == billing) {
				JSONObject object = new JSONObject(customData);
				if (!object.has(RecurringPaymentTransactionTypeConstants.CLIENTID)) {
					recurringBillingHistory.setClientId(0L);
					recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
					recurringBillingHistory.setObsDescription("clientId Not Found in the custom Parameter Data");
					this.recurringBillingHistoryRepository.save(recurringBillingHistory);
					return null;
				}
				
				final Long clientId = object.getLong(RecurringPaymentTransactionTypeConstants.CLIENTID);

				billing = new RecurringBilling(clientId, ProfileId, RecurringPaymentTransactionTypeConstants.GATEWAY_NAME_PAYPAL);
				this.recurringBillingRepositoryWrapper.save(billing);

				String currencyCode = null;

				if (request.getParameterMap().containsKey(RecurringPaymentTransactionTypeConstants.MC_CURRENCY)) {
					currencyCode = request.getParameter(RecurringPaymentTransactionTypeConstants.MC_CURRENCY);
				}

				updateRecurring(ProfileId, 0, null, currencyCode);

				recurringBillingHistory.setClientId(clientId);
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
				recurringBillingHistory.setObsDescription("Recurring Billing Profile Successfully Created, ProfileId=" + ProfileId);
				this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			}
			return billing;

		} catch (JSONException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("Profile Creation Failed Due to JsonException on custom Parameter Data," + " Reason:=" + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			return null;
		} catch (Exception e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("Profile Creation Failed Due to Exception Throwing," + " Reason:=" + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			return null;
		}
	}

	/**
	 * Using this createJsonForOnlineMethod(HttpServletRequest request) Method
	 * for Preparing JsonData For PaymentGatewayApiResource.java's
	 * OnlinePaymentMethod(final String apiRequestBodyAsJson).
	 * 
	 * @return JsonData String to process Payment for the Client.
	 */
	public String createJsonForOnlineMethod(HttpServletRequest request) throws JSONException {

		String status;

		final String paymentStatus = request.getParameter(RecurringPaymentTransactionTypeConstants.PAYMENTSTATUS);
		final String currency = request.getParameter(RecurringPaymentTransactionTypeConstants.MCCURRENCY);
		final BigDecimal amount = new BigDecimal(request.getParameter(RecurringPaymentTransactionTypeConstants.MCGROSS));
		final String date = dateFormat.format(new Date());

		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("paymentDate", date);
		jsonObj.addProperty("payerEmail", request.getParameter("payer_email"));
		jsonObj.addProperty("customer_name", request.getParameter("first_name"));
		jsonObj.addProperty("receiverEmail", request.getParameter("receiver_email"));
		jsonObj.addProperty("payerStatus", request.getParameter("payer_status"));
		jsonObj.addProperty("currency", currency);
		jsonObj.addProperty("paymentStatus", paymentStatus);

		final String customData = customDataReturn(request);

		JSONObject jsonObject = new JSONObject(customData);
		
		final Long clientId = jsonObject.getLong(RecurringPaymentTransactionTypeConstants.CLIENTID);

		jsonObject = new JSONObject();

		if (paymentStatus.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.COMPLETED)) {
			status = RecurringPaymentTransactionTypeConstants.SUCCESS;
		} else if (paymentStatus.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.PENDING)) {
			status = RecurringPaymentTransactionTypeConstants.PENDING;
			final String error = request.getParameter(RecurringPaymentTransactionTypeConstants.PENDINGREASON);
			jsonObj.addProperty("pendingReason", error);
			jsonObject.put("error", error);
		} else {
			status = paymentStatus;
		}

		jsonObject.put("source", RecurringPaymentTransactionTypeConstants.PAYPAL);
		jsonObject.put("transactionId", request.getParameter(RecurringPaymentTransactionTypeConstants.TRANSACTIONID));
		jsonObject.put("currency", currency);
		jsonObject.put("clientId", clientId);
		jsonObject.put("total_amount", amount);
		jsonObject.put("locale", "en");
		jsonObject.put("dateFormat", "dd MMMM yyyy");
		jsonObject.put("otherData", jsonObj.toString());
		jsonObject.put("status", status);

		return jsonObject.toString();

	}

	/**
	 * We can use this Method to Updating Paypal Recurring Profile Using
	 * SubscriberId.
	 * 
	 * @param maxFailedPayments
	 *            --> Adding maxFailedPayments value to Profile.
	 * @param subscriberId
	 *            --> subscriberId of the Paypal Recurring Table.
	 * @param billingCycles
	 *            --> Adding Additional Billing Cycles to Existing Profile.
	 * @param totalAmount
	 *            --> Updating the Recurring Profile Price to this Parameter
	 *            value. NOTE: This Updation Only Work, When the Increasing
	 *            Price is lessthan 20% to Profile Price.
	 * @param currency
	 * @return
	 */
	private Map<String, Object> updateRecurring(final String subscriberId, final int billingCycles, 
			final String totalAmount, final String currencyCode) {

		final Map<String, Object> mapDetails = new HashMap<String, Object>();

		try {
			final Configuration config = this.configurationRepository.findOneByName(ConfigurationConstants.PAYPAL_MAX_FAILED_COUNT);
			int maxFailedPayments = 5;

			if (null != config && config.isEnabled() && !config.getValue().isEmpty()) {
				maxFailedPayments = Integer.parseInt(config.getValue());
			}

			final UpdateRecurringPaymentsProfileRequestDetailsType updateRecurringPaymentsProfileRequestDetails = new UpdateRecurringPaymentsProfileRequestDetailsType();
			updateRecurringPaymentsProfileRequestDetails.setProfileID(subscriberId);

			if (billingCycles > 0) {
				updateRecurringPaymentsProfileRequestDetails.setAdditionalBillingCycles(billingCycles);
			}

			if (null != totalAmount) {
				BasicAmountType amount = new BasicAmountType();
				amount.setCurrencyID(CurrencyCodeType.valueOf(currencyCode));
				amount.setValue(totalAmount);
				updateRecurringPaymentsProfileRequestDetails.setAmount(amount);
			}

			updateRecurringPaymentsProfileRequestDetails.setMaxFailedPayments(maxFailedPayments);
			UpdateRecurringPaymentsProfileRequestType updateRecurringPaymentsProfileRequest = new UpdateRecurringPaymentsProfileRequestType();
			updateRecurringPaymentsProfileRequest.setUpdateRecurringPaymentsProfileRequestDetails(updateRecurringPaymentsProfileRequestDetails);
			UpdateRecurringPaymentsProfileReq profile = new UpdateRecurringPaymentsProfileReq();
			profile.setUpdateRecurringPaymentsProfileRequest(updateRecurringPaymentsProfileRequest);

			// Create the PayPalAPIInterfaceServiceService service object to make the API call
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(getMapConfigDetails());
			UpdateRecurringPaymentsProfileResponseType manageProfileStatusResponse = service.updateRecurringPaymentsProfile(profile);

			if (manageProfileStatusResponse.getAck().equals(AckCodeType.FAILURE)
					|| (manageProfileStatusResponse.getErrors() != null && manageProfileStatusResponse.getErrors().size() > 0)) {
				String error = manageProfileStatusResponse.getErrors().get(0).getLongMessage();
				logger.info("Paypal Profile Update Failed due to Reason:" + error);
				mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				mapDetails.put("error", error);
			} else {
				logger.info("Paypal Profile Update Successfully Completed.");
				mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
				mapDetails.put("error", "");
			}
		} catch (JSONException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (SSLConfigurationException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (InvalidCredentialException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (HttpErrorException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (InvalidResponseDataException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (ClientActionRequiredException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (MissingCredentialException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (OAuthException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (IOException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (InterruptedException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (ParserConfigurationException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (SAXException e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		} catch (Exception e) {
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		}

		return mapDetails;
	}

	/**
	 * 
	 * We are Internally Using updateRecurring(String subscriberId, int
	 * billingCycles, String totalAmount) Method to Change/Update the Recurring
	 * Profile.
	 */
	public CommandProcessingResult updatePaypalRecurring(final JsonCommand command) {
	
		Map<String, Object> mapDetails;

		try {
			int billingCycles = 0;
			String totalAmount = null, currencyCode = "USD";
			
			final Long orderId = command.longValueOfParameterNamed("orderId");
			final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneByOrderId(orderId);
			if (null == recurringBilling)
				throw new OrderNotFoundException(orderId);

			if (command.hasParameter("billingCycles"))
				billingCycles = command.integerValueOfParameterNamed("billingCycles");

			if (command.hasParameter("totalAmount"))
				totalAmount = command.stringValueOfParameterNamed("totalAmount");

			if (command.hasParameter("currencyCode"))
				currencyCode = command.stringValueOfParameterNamed("currencyCode");

			mapDetails = updateRecurring(recurringBilling.getSubscriberId(), billingCycles, totalAmount, currencyCode);

		} catch (Exception e) {
			mapDetails = new HashMap<>();
			mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			mapDetails.put("error", stackTraceToString(e));
		}

		return new CommandProcessingResultBuilder().with(mapDetails).build();
	}

	/**
	 * Using this Method we are Updating Paypal Profile Status to "Active" or
	 * "Suspend" or "ReActive".
	 * 
	 * @param recurringStatus
	 *            Parameter value must get From JsonObject.
	 * 
	 * @throw OrderNotFoundException(orderId) exception, when the Order is not
	 *        found with this orderId
	 * 
	 * @throw PaypalStatusChangeActionTypeMisMatchException(status) exception,
	 *        when the StatusChangeActionType Enum Value is not found with this
	 *        status
	 */

	public CommandProcessingResult updatePaypalProfileStatus(JsonCommand command) {

		final Map<String, Object> mapDetails = new HashMap<String, Object>();

		try {
			final Long orderId = command.longValueOfParameterNamed("orderId");
			final String status = command.stringValueOfParameterNamed("recurringStatus");
			final RecurringBilling recurringBilling = this.recurringBillingRepositoryWrapper.findOneByOrderId(orderId);

			if (null == recurringBilling) {
				throw new OrderNotFoundException(orderId);
			}

			final StatusChangeActionType statusChangeActionType = StatusChangeActionType.valueOf(status);

			if (null == statusChangeActionType) {
				throw new PaypalStatusChangeActionTypeMisMatchException(status);
			}

			ManageRecurringPaymentsProfileStatusRequestType request = new ManageRecurringPaymentsProfileStatusRequestType();
			ManageRecurringPaymentsProfileStatusRequestDetailsType details = new ManageRecurringPaymentsProfileStatusRequestDetailsType();
			request.setManageRecurringPaymentsProfileStatusRequestDetails(details);
			details.setProfileID(recurringBilling.getSubscriberId());
			details.setAction(statusChangeActionType);

			// Invoke the API
			ManageRecurringPaymentsProfileStatusReq wrapper = new ManageRecurringPaymentsProfileStatusReq();
			wrapper.setManageRecurringPaymentsProfileStatusRequest(request);

			// Create the PayPalAPIInterfaceServiceService service object to
			// make the API call
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(getMapConfigDetails());

			ManageRecurringPaymentsProfileStatusResponseType manageProfileStatusResponse = service.manageRecurringPaymentsProfileStatus(wrapper);

			if (manageProfileStatusResponse.getAck().equals(AckCodeType.SUCCESS)
					|| manageProfileStatusResponse.getAck().equals(AckCodeType.SUCCESSWITHWARNING)) {

				mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
				mapDetails.put("acknoledgement", manageProfileStatusResponse.getAck());
				mapDetails.put("error", "");

			} else if (manageProfileStatusResponse.getAck().equals(AckCodeType.FAILURE)
					|| manageProfileStatusResponse.getAck().equals(AckCodeType.FAILUREWITHWARNING)
					|| (manageProfileStatusResponse.getErrors() != null && manageProfileStatusResponse.getErrors().size() > 0)) {

				final String error = manageProfileStatusResponse.getErrors().get(0).getLongMessage();

				mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				mapDetails.put("acknoledgement", manageProfileStatusResponse.getAck());
				mapDetails.put("error", error);

			} else {
				mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				mapDetails.put("acknoledgement", manageProfileStatusResponse.getAck());
				mapDetails.put("error", "");
			}

			return new CommandProcessingResultBuilder().with(mapDetails).build();

		} catch (JSONException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (SSLConfigurationException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (InvalidCredentialException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (HttpErrorException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (InvalidResponseDataException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (ClientActionRequiredException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (MissingCredentialException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (OAuthException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (IOException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (InterruptedException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (ParserConfigurationException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (SAXException e) {
			mapDetails.put("error", stackTraceToString(e));
		} catch (Exception e) {
			mapDetails.put("error", stackTraceToString(e));
		}

		mapDetails.put("result", RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
		mapDetails.put("acknoledgement", "");

		return new CommandProcessingResultBuilder().with(mapDetails).build();
	}

	/**
	 * Note: Usually this Method is call when the "MaxFailedPayment" Event
	 * Fired.
	 * 
	 * Using this disConnectOrder(String profileId) Method to Disconnect the
	 * Order using subscriberId.
	 * 
	 * @return getOrderIdFromSubscriptionId(profileId) Method returns orderId
	 *         From b_recurring table.
	 * 
	 * @return getOrderStatus(orderId) Method returns status of the Order. if
	 *         Status id Active then only We perform Disconnect Operation.
	 * 
	 */
	public void paypalDisConnectOrder(final Long orderId, final String profileId,
			final RecurringBillingHistory recurringBillingHistory) {

		final String date = dateFormat.format(new Date());
		JSONObject object = new JSONObject();

		try {

			final String status = getOrderStatus(orderId);

			if (null != status && status.equalsIgnoreCase(StatusTypeEnum.ACTIVE.toString())) {
				Order order = this.orderRepository.findOne(orderId);
				object.put("disconnectionDate", date);
				object.put("disconnectReason", "DisConnecting this Order Based on Paypal Recurring Billing Instruction");
				final CommandWrapper commandRequest = new CommandWrapperBuilder().updateOrder(orderId).withJson(object.toString()).build();
				final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);

				if (result != null && result.resourceId() > 0) {
					recurringBillingHistory.setClientId(order.getClientId());
					recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
					recurringBillingHistory.setObsDescription("Order DisConnection Completed...");
				} else {
					recurringBillingHistory.setClientId(order.getClientId());
					recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
					recurringBillingHistory.setObsDescription("Order DisConnection Failed...");
				}
			} else {
				Order order = this.orderRepository.findOne(orderId);
				recurringBillingHistory.setClientId(order.getClientId());
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_UNKNOWN);
				recurringBillingHistory.setObsDescription("Status of Order=" + status);
			}

		} catch (JSONException e) {
			logger.info("JsonException: " + e.getMessage());
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("JsonException Order Termination Failed, Reason: " + stackTraceToString(e));
		}

		this.recurringBillingHistoryRepository.save(recurringBillingHistory);
	}

	@Override
	public String getOrderStatus(final Long orderId) {

		final Order order = this.orderRepository.findOne(orderId);
		if (order == null) {
			logger.info("order Not found with this orderId:" + orderId);
			return null;
		}
		final EnumOptionData Enumstatus = OrderStatusEnumaration.OrderStatusType(order.getStatus().intValue());
		return Enumstatus.getValue();
	}

	@Override
	public RecurringBilling getRecurringBillingObject(final String profileId) {

		final RecurringBilling billing = this.recurringBillingRepositoryWrapper.findOneBySubscriberId(profileId);

		if (billing == null) {
			logger.info("Recurring Billing Profile Not found with this SubscriberId:" + profileId);
			return null;
		}

		return billing;
	}

	private Map<String, String> getMapConfigDetails() throws JSONException {

		final String value = this.paymentGatewayConfigurationRepositoryWrapper.getValue(ConfigurationConstants.PAYPAL_RECURRING_PAYMENT_DETAILS);

		final JSONObject pgConfigObject = new JSONObject(value);
		final String username = pgConfigObject.getString("userName");
		final String password = pgConfigObject.getString("password");
		final String signature = pgConfigObject.getString("signature");
		final String serverType = pgConfigObject.getString("serverType");

		final Map<String, String> sdkConfig = new HashMap<String, String>();
		sdkConfig.put(RecurringPaymentTransactionTypeConstants.SERVER_MODE, serverType);
		sdkConfig.put(RecurringPaymentTransactionTypeConstants.API_USERNAME, username);
		sdkConfig.put(RecurringPaymentTransactionTypeConstants.API_PASSWORD, password);
		sdkConfig.put(RecurringPaymentTransactionTypeConstants.API_SIGNATURE, signature);

		return sdkConfig;
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
	 * Using this Method we getCustom Data.
	 * 
	 * Custom Parameter is exist in the Parameters List, if we do Payment with
	 * Paypal site through WEB.
	 * 
	 * rp_invoice_id Parameter is exist in the Parameter List, if and only if we
	 * Pay the Money with Paypal Api Internally.
	 * 
	 * Note: in the Case of using Paypal Api, We didnot have the Custom
	 * Parameter to set the UserDefined Data. So we are Saving the CustomData in
	 * the EventAction table and get the Id of that Record and send that id as
	 * invoice_id in Paypal Api.
	 * 
	 * 
	 */
	private String customDataReturn(HttpServletRequest request) {

		if (request.getParameterMap().containsKey(RecurringPaymentTransactionTypeConstants.CUSTOM)) {
			return request.getParameter(RecurringPaymentTransactionTypeConstants.CUSTOM);
		} else if (request.getParameterMap().containsKey(RecurringPaymentTransactionTypeConstants.RP_INVOICE_ID)) {
			final String eventID = request.getParameter(RecurringPaymentTransactionTypeConstants.RP_INVOICE_ID);
			final EventAction eventAction = this.eventActionRepository.findOne(new Long(eventID));

			if (null != eventAction)
				return eventAction.getCommandAsJson();
			else
				return null;
		} else {
			return null;
		}
	}
}
