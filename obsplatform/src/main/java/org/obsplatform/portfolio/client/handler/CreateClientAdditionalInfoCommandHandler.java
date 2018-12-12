package org.obsplatform.portfolio.client.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.ownedhardware.service.OwnedHardwareWritePlatformService;
import org.obsplatform.portfolio.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateClientAdditionalInfoCommandHandler implements NewCommandSourceHandler {

	
	private final ClientWritePlatformService clientWritePlatformService;
	
	@Autowired
	public CreateClientAdditionalInfoCommandHandler(final ClientWritePlatformService clientWritePlatformService) {
		this.clientWritePlatformService = clientWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return clientWritePlatformService.createClientAdditionalInfo(command, command.entityId());
	}

}
