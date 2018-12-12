package org.obsplatform.portfolio.property.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.property.service.PropertyWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePropertyCommandHandler implements NewCommandSourceHandler {

	private final PropertyWriteplatformService propertyWriteplatformService;

	@Autowired
	public DeletePropertyCommandHandler(final PropertyWriteplatformService propertyWriteplatformService) {

		this.propertyWriteplatformService = propertyWriteplatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.propertyWriteplatformService.deleteProperty(command.entityId());
	}
}
