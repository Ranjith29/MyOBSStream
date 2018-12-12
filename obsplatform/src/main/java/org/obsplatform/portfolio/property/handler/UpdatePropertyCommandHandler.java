package org.obsplatform.portfolio.property.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.property.service.PropertyWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdatePropertyCommandHandler implements NewCommandSourceHandler {

	private final PropertyWriteplatformService writePlatformService;

	@Autowired
	public UpdatePropertyCommandHandler(final PropertyWriteplatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.updateProperty(command.entityId(),command);
	}
}