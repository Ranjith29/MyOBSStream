package org.obsplatform.portfolio.activationprocess.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.activationprocess.service.ActivationProcessWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSelfRegistrationCommandHandler implements NewCommandSourceHandler {

	private final ActivationProcessWritePlatformService activationProcessWritePlatformService;

    @Autowired
    public CreateSelfRegistrationCommandHandler(final ActivationProcessWritePlatformService activationProcessWritePlatformService) {
        this.activationProcessWritePlatformService = activationProcessWritePlatformService;
    }

    @Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
    	
    	 return this.activationProcessWritePlatformService.selfRegistrationProcess(command);
	}

}
