package org.obsplatform.infrastructure.configuration.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.configuration.service.ConfigurationWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateSmtpConfigurationCommandHandler  implements NewCommandSourceHandler {

	private final ConfigurationWritePlatformService writePlatformService;
	  
	  @Autowired
	    public CreateSmtpConfigurationCommandHandler(ConfigurationWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	       
	    }

	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return writePlatformService.create(command);
	}
}
