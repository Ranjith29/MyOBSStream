package org.obsplatform.finance.payments.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditcardPaymentCommandHandler implements NewCommandSourceHandler {

	
	private PaymentGatewayWritePlatformService writePlatformService;



	@Autowired
	public CreditcardPaymentCommandHandler(final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService)
	{
	this.writePlatformService =paymentGatewayWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.crediCardProcess(command);
	}

}



