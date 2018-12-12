package org.obsplatform.finance.paymentsgateway.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 * 
 */
@Service
public class OnlinePaymentGatewayCommandhandler implements NewCommandSourceHandler {

	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;

	@Autowired
	public OnlinePaymentGatewayCommandhandler(
			final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService) {
		this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.paymentGatewayWritePlatformService.onlinePaymentGateway(command);
	}
}
