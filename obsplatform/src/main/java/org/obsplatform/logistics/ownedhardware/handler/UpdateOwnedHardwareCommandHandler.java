package org.obsplatform.logistics.ownedhardware.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.ownedhardware.service.OwnedHardwareWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOwnedHardwareCommandHandler implements NewCommandSourceHandler {

	
	OwnedHardwareWritePlatformService ownedHardwareWritePlatformService;
	
	@Autowired
	public UpdateOwnedHardwareCommandHandler(final OwnedHardwareWritePlatformService ownedHardwareWritePlatformService) {
		this.ownedHardwareWritePlatformService = ownedHardwareWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return ownedHardwareWritePlatformService.updateOwnedHardware(command, command.entityId());
	}

}
