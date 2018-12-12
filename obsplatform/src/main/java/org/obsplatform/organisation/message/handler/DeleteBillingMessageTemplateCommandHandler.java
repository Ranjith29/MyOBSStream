package org.obsplatform.organisation.message.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.message.service.BillingMessageTemplateWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class DeleteBillingMessageTemplateCommandHandler implements NewCommandSourceHandler {

	private final BillingMessageTemplateWritePlatformService billingMessageTemplateWritePlatformService;
	
	@Autowired
	public DeleteBillingMessageTemplateCommandHandler(final BillingMessageTemplateWritePlatformService billingMessageTemplateWritePlatformService)
	{
	this.billingMessageTemplateWritePlatformService =billingMessageTemplateWritePlatformService;
	}
	
	 @Transactional
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.billingMessageTemplateWritePlatformService.deleteMessageTemplate(command);
	}

}
