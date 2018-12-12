package org.obsplatform.organisation.feemaster.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.feemaster.service.FeeMasterWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFeeMasterCommandHandler implements NewCommandSourceHandler {

	private final FeeMasterWriteplatformService feeMasterWriteplatformService;

	@Autowired
	public CreateFeeMasterCommandHandler(final FeeMasterWriteplatformService feeMasterWriteplatformService) {

		this.feeMasterWriteplatformService = feeMasterWriteplatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.feeMasterWriteplatformService.createFeeMaster(command);
	}
}
