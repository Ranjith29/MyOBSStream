package org.obsplatform.organisation.message.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.message.service.BillingMessageDataWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class CreateMessageDataCommandHandler implements NewCommandSourceHandler{
	
	private final BillingMessageDataWritePlatformService billingmessageDataWritePlatformService;
	
	@Autowired
	public CreateMessageDataCommandHandler(final BillingMessageDataWritePlatformService billingmessageDataWritePlatformService)
	{
	this.billingmessageDataWritePlatformService =billingmessageDataWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		return this.billingmessageDataWritePlatformService.createMessageData(command.entityId(),command.json());
	}

}
