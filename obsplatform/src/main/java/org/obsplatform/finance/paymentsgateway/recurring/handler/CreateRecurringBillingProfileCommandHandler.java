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
public class CreateRecurringBillingProfileCommandHandler implements NewCommandSourceHandler {

	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	@Autowired
	public CreateRecurringBillingProfileCommandHandler(final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService) {
		this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.paymentGatewayRecurringWritePlatformService.createRecurringBilling(command);
	}
}
