package org.obsplatform.finance.paymentsgateway.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayConfigurationWritePlatformService;
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
public class UpdatePaymentGatewayConfigCommandHandler implements NewCommandSourceHandler {

	private final PaymentGatewayConfigurationWritePlatformService paymentGatewayWritePlatformService;

	@Autowired
	public UpdatePaymentGatewayConfigCommandHandler(
			final PaymentGatewayConfigurationWritePlatformService paymentGatewayWritePlatformService) {
		this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {

		return this.paymentGatewayWritePlatformService.updatePaymentGatewayConfig(command.entityId(), command);
	}
	
}
