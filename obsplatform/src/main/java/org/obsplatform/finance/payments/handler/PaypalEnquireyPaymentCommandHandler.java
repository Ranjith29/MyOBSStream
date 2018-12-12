package org.obsplatform.finance.payments.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.payments.service.PaymentWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaypalEnquireyPaymentCommandHandler implements NewCommandSourceHandler {

	private final PaymentWritePlatformService writePlatformService;

	@Autowired
	public PaypalEnquireyPaymentCommandHandler(final PaymentWritePlatformService writePlatformService) {
		
		this.writePlatformService = writePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return this.writePlatformService.paypalEnquirey(command);
	}

}
