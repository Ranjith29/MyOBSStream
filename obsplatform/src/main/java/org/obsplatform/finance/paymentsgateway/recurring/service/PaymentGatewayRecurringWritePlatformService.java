package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentGatewayRecurringWritePlatformService {

	public String paypalRecurringVerification(final HttpServletRequest request) throws UnsupportedEncodingException,
			IllegalStateException, ClientProtocolException, IOException, JSONException;

	// public String getAccessToken(String data);

	public void recurringEventUpdate(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) throws JSONException;

	public RecurringBilling recurringSubscriberSignUp(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory);

	public String createJsonForOnlineMethod(final HttpServletRequest request) throws JSONException;

	public CommandProcessingResult updatePaypalRecurring(final JsonCommand command);

	public CommandProcessingResult updatePaypalProfileStatus(final JsonCommand command);

	public void disConnectOrder(final String profileId, final RecurringBillingHistory recurringBillingHistory);

	public Long updateRecurringBillingTable(final String profileId);

	public CommandProcessingResult deleteRecurringBilling(final JsonCommand command);

	public String getRequestParameters(final HttpServletRequest request);

	public CommandProcessingResult processAuthorizeRecurringBillingProfile(final JsonCommand command);

	public CommandProcessingResult updateRecurringBillingProfileOrderId(final JsonCommand command);

	//public CommandProcessingResult notifyAuthorizeRecurringBillingRequest(JsonCommand command);
	public String notifyAuthorizeRecurringBillingRequest(final String jsonCommand);

	public CommandProcessingResult createRecurringBilling(final JsonCommand command);

}
