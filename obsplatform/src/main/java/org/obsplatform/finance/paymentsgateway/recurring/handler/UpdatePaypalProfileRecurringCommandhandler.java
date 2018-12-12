package org.obsplatform.finance.paymentsgateway.recurring.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.recurring.service.PaymentGatewayRecurringWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 * 
 */
@Service
public class UpdatePaypalProfileRecurringCommandhandler implements NewCommandSourceHandler {

	private final PaymentGatewayRecurringWritePlatformService paymentGatewayWritePlatformService;

	@Autowired
	public UpdatePaypalProfileRecurringCommandhandler(final PaymentGatewayRecurringWritePlatformService paymentGatewayWritePlatformService) {
		this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.paymentGatewayWritePlatformService.updatePaypalRecurring(command);
	}

}
