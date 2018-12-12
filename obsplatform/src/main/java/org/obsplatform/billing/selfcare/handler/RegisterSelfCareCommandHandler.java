package org.obsplatform.billing.selfcare.handler;

import org.obsplatform.billing.selfcare.service.SelfCareWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSelfCareCommandHandler implements NewCommandSourceHandler{

	private final SelfCareWritePlatformService selfCareWritePlatformService; 
	
	@Autowired
	public RegisterSelfCareCommandHandler(final SelfCareWritePlatformService selfCareWritePlatformService) {
		this.selfCareWritePlatformService = selfCareWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return selfCareWritePlatformService.registerSelfCare(command);
	}

}
