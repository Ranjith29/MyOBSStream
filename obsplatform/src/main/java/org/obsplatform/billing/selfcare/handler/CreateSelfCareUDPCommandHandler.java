package org.obsplatform.billing.selfcare.handler;


import org.obsplatform.billing.selfcare.service.SelfCareWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateSelfCareUDPCommandHandler implements NewCommandSourceHandler{

	
	private final SelfCareWritePlatformService selfCareWritePlatformService; 
	
	@Autowired
	public CreateSelfCareUDPCommandHandler(final SelfCareWritePlatformService selfCareWritePlatformService) {
		this.selfCareWritePlatformService = selfCareWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return selfCareWritePlatformService.createSelfCareUDPassword(command);
	}
}

