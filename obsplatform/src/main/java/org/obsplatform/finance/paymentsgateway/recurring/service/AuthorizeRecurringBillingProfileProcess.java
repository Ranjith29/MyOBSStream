/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.service;


/**
 * @author ashokreddy
 *
 */
public interface AuthorizeRecurringBillingProfileProcess {

	public String createARBProfile(final String jsonData);

	public String cancelARBProfile(final String jsonData);

	public String notifyAuthorizeRequest(final String jsonData);

	public String updateARBProfile(final String jsonData);

	public String getARBProfile(final String json);
}
