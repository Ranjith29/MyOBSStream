package org.obsplatform.finance.usagecharges.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.finance.usagecharges.service.UsageChargesWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUsageChargesRawDataCommandHandler implements NewCommandSourceHandler {

	private final UsageChargesWritePlatformService writePlatformService;

	@Autowired
	public CreateUsageChargesRawDataCommandHandler(final UsageChargesWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.createUsageChargesRawData(command);
	}
	
	
}
