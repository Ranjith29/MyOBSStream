/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * @author ashokreddy
 *
 */
public class RecurringBillingNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecurringBillingNotFoundException(final String profileId) {
		super("error.msg.paymentgateway.recurring.profile.not.found",
				" Recurring Profile Not found with this profileId: " + profileId, profileId);
	}

	public RecurringBillingNotFoundException(final Long orderId) {
		super("error.msg.paymentgateway.recurring.profile.not.found",
				" Recurring Profile Not found with this orderId: " + orderId, orderId);
	}

	public RecurringBillingNotFoundException(final String profileId, final Long clientId) {
		super("error.msg.paymentgateway.recurring.profile.not.found",
				" Recurring Profile Not found with this profileId : " + profileId + " and clientId : " + clientId,
				profileId, clientId);
	}
}
