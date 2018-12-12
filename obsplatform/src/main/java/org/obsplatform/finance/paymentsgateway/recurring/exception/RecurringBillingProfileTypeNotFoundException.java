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
public class RecurringBillingProfileTypeNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecurringBillingProfileTypeNotFoundException(Long orderId) {
		super("error.msg.paymentgateway.recurring.profile.type.not.found",
				" Recurring Profile Type(GatewayName) Not found with this orderId: " + orderId, orderId);
	}

	public RecurringBillingProfileTypeNotFoundException(String gatewayName) {
		super("error.msg.recurring.gateway.not.implement", gatewayName + " cancel Profile is implementing. ",
				gatewayName);
	}

}
