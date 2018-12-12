package org.obsplatform.cms.epgprogramguide.handler;

import org.obsplatform.cms.epgprogramguide.service.EpgProgramGuideWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CreateEpgProgramGuideCommandHandler implements NewCommandSourceHandler {

	private EpgProgramGuideWritePlatformService epgProgramGuideWritePlatformService;
	
	@Autowired
	public CreateEpgProgramGuideCommandHandler(final EpgProgramGuideWritePlatformService epgProgramGuideWritePlatformService) {
		this.epgProgramGuideWritePlatformService = epgProgramGuideWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.epgProgramGuideWritePlatformService.createEpgProgramGuide(command,command.entityId());
	}

}

