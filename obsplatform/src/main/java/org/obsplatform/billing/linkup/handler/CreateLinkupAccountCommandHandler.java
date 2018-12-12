package org.obsplatform.billing.linkup.handler;

import org.obsplatform.billing.linkup.service.LinkupAccountWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateLinkupAccountCommandHandler implements NewCommandSourceHandler{

	private final LinkupAccountWritePlatformService linkupAccountWritePlatformService; 
	
	@Autowired
	public CreateLinkupAccountCommandHandler(final LinkupAccountWritePlatformService linkupAccountWritePlatformService) {
		this.linkupAccountWritePlatformService = linkupAccountWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return linkupAccountWritePlatformService.createLinkupAccount(command);
	}
}

