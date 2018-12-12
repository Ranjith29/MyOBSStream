/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingHistory;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * @author ashokreddy
 *
 */
public interface PaypalRecurringBillingProfileProcess {

	public void paypalDisConnectOrder(final Long orderId, final String profileId,
			final RecurringBillingHistory recurringBillingHistory);

	public String recurringVerification(final HttpServletRequest request)
			throws IllegalStateException, ClientProtocolException, IOException, JSONException;

	public void recurringProfileUpdate(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory) throws JSONException;

	public RecurringBilling recurringProfileCreation(final HttpServletRequest request,
			final RecurringBillingHistory recurringBillingHistory);

	public String createJsonForOnlineMethod(final HttpServletRequest request) throws JSONException;

	public CommandProcessingResult updatePaypalRecurring(final JsonCommand command);

	public CommandProcessingResult updatePaypalProfileStatus(final JsonCommand command);

	public RecurringBilling getRecurringBillingObject(final String profileId);

	public String getOrderStatus(final Long orderId);

}
