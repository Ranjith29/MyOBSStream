/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.finance.paymentsgateway.recurring.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.recurring.service.PaymentGatewayRecurringWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ashokreddy
 *
 */
@Service
public class UpdateRecurringBillingOrderIdCommandHandler implements NewCommandSourceHandler {

	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	@Autowired
	public UpdateRecurringBillingOrderIdCommandHandler(final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService) {
		this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.paymentGatewayRecurringWritePlatformService.updateRecurringBillingProfileOrderId(command);
	}
}
