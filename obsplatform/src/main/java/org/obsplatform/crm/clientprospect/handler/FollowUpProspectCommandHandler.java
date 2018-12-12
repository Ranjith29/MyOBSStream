package org.obsplatform.crm.clientprospect.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.crm.clientprospect.service.ClientProspectWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FollowUpProspectCommandHandler implements NewCommandSourceHandler {

	private ClientProspectWritePlatformService clientProspectWritePlatformService;

	@Autowired
	public FollowUpProspectCommandHandler(final ClientProspectWritePlatformService clientProspectWritePlatformService) {
		this.clientProspectWritePlatformService = clientProspectWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return clientProspectWritePlatformService.followUpProspect(command, command.entityId());
	}

}
