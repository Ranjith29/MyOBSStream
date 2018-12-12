package org.obsplatform.provisioning.provisioning.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmProvisioningDetailsCommandHandler implements
		NewCommandSourceHandler {

	private final ProvisioningWritePlatformService writePlatformService;

	@Autowired
	public ConfirmProvisioningDetailsCommandHandler(
			final ProvisioningWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.confirmProvisioningDetails(command
				.entityId());
	}
}
