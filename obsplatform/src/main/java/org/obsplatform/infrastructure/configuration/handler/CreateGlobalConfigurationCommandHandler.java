package org.obsplatform.infrastructure.configuration.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.configuration.service.GlobalConfigurationWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateGlobalConfigurationCommandHandler  implements NewCommandSourceHandler {

	private final GlobalConfigurationWritePlatformService writePlatformService;
	  
	  @Autowired
	    public CreateGlobalConfigurationCommandHandler(GlobalConfigurationWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	       
	    }

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return writePlatformService.create(command);
	}
}
