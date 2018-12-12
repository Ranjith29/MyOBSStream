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
public class AuthorizeRecurringBillingCommandHandler implements NewCommandSourceHandler {

	private final PaymentGatewayRecurringWritePlatformService writePlatformService;

	@Autowired
	public AuthorizeRecurringBillingCommandHandler(final PaymentGatewayRecurringWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.processAuthorizeRecurringBillingProfile(command);
	}
}
