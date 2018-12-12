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
public class RecurringProfileNotMatchingException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecurringProfileNotMatchingException(final Long orderId, final String gatewayNameAuthorizenet) {
		super("error.msg.recurring.authorize.profile.not.match",
				" Recurring Profile's gatewayName of this orderId: " + orderId
						+ " is doesn't match with AuthorizeNet gatewayName:" + gatewayNameAuthorizenet,
				orderId, gatewayNameAuthorizenet);
	}
	
	public RecurringProfileNotMatchingException(final String subscriberId, final String gatewayNameAuthorizenet) {
		super("error.msg.recurring.authorize.profile.not.match",
				" Recurring Profile's gatewayName of this subscriberId: " + subscriberId					
				+ " is doesn't match with AuthorizeNet gatewayName:" + gatewayNameAuthorizenet,
				subscriberId, gatewayNameAuthorizenet);
	}

}
