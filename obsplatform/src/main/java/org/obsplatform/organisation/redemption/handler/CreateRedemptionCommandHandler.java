package org.obsplatform.organisation.redemption.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.redemption.service.RedemptionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRedemptionCommandHandler implements NewCommandSourceHandler {

	private final RedemptionWritePlatformService redemptionWritePlatformService;
	
	@Autowired
	public CreateRedemptionCommandHandler(final RedemptionWritePlatformService redemptionWritePlatformService){
		this.redemptionWritePlatformService = redemptionWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return this.redemptionWritePlatformService.createRedemption(command);
	}

}
