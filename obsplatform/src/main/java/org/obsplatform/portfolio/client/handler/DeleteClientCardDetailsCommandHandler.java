package org.obsplatform.portfolio.client.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.client.service.ClientCardDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteClientCardDetailsCommandHandler implements NewCommandSourceHandler {

	
	 private final ClientCardDetailsWritePlatformService clientCardDetailsWritePlatformService;

	    @Autowired
	    public DeleteClientCardDetailsCommandHandler(ClientCardDetailsWritePlatformService clientCardDetailsWritePlatformService) {
	        this.clientCardDetailsWritePlatformService = clientCardDetailsWritePlatformService;
	    }

	    @Transactional
		@Override
		public CommandProcessingResult processCommand(JsonCommand command) {
	    	return this.clientCardDetailsWritePlatformService.deleteClientCardDetails(command);
		}
	    
}
