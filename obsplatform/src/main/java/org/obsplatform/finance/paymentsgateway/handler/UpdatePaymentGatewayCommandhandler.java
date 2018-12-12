package org.obsplatform.finance.paymentsgateway.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
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
public class UpdatePaymentGatewayCommandhandler implements NewCommandSourceHandler {

	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;

	@Autowired
	public UpdatePaymentGatewayCommandhandler(
			final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService) {
		this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {

		return this.paymentGatewayWritePlatformService.updatePaymentGateway(command);
	}

}
