package org.obsplatform.organisation.hardwareplanmapping.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.hardwareplanmapping.service.HardwarePlanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateHardwarePlanCommandHandler implements
		NewCommandSourceHandler {

	private final HardwarePlanWritePlatformService writePlatformService;

	@Autowired
	public CreateHardwarePlanCommandHandler(final HardwarePlanWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.createHardwarePlan(command);
	}
}