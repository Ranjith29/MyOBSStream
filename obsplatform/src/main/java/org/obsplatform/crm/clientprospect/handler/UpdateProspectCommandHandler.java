package org.obsplatform.crm.clientprospect.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.crm.clientprospect.service.ClientProspectWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProspectCommandHandler implements NewCommandSourceHandler {

	private final ClientProspectWritePlatformService clientProspectWritePlatformService;

	@Autowired
	public UpdateProspectCommandHandler(final ClientProspectWritePlatformService clientProspectWritePlatformService) {
		
		this.clientProspectWritePlatformService = clientProspectWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return this.clientProspectWritePlatformService.updateProspect(command);
	}
}
