package org.obsplatform.billing.promotioncodes.handler;

import org.obsplatform.billing.promotioncodes.service.PromotionCodeWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePromotionCodeCommandHandler implements
		NewCommandSourceHandler {

	private final PromotionCodeWritePlatformService writePlatformService;

	@Autowired
	public DeletePromotionCodeCommandHandler(
			final PromotionCodeWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.deletePromotionCode(command.entityId());
	}
}