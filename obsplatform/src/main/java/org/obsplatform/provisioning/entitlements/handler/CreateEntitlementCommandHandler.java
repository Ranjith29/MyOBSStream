package org.obsplatform.provisioning.entitlements.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.entitlements.service.EntitlementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEntitlementCommandHandler implements NewCommandSourceHandler {

	private final EntitlementWritePlatformService writePlatformService;
	
	@Autowired
	public CreateEntitlementCommandHandler(EntitlementWritePlatformService writePlatformService){
		this.writePlatformService=writePlatformService;
		
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.create(command);
	}
}
