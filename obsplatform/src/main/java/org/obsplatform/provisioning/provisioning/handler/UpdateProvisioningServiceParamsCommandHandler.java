package org.obsplatform.provisioning.provisioning.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.provisioning.service.ProvisioningServiceParamsWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateProvisioningServiceParamsCommandHandler implements NewCommandSourceHandler {

	 private final ProvisioningServiceParamsWriteplatformService writePlatformService;

	    @Autowired
	    public UpdateProvisioningServiceParamsCommandHandler(final ProvisioningServiceParamsWriteplatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	    }

		@Override
		public CommandProcessingResult processCommand(JsonCommand command) {
	       return this.writePlatformService.updateServiceParams(command,command.entityId());
		}
}
