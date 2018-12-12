package org.obsplatform.cms.epgprogramguide.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EpgProgramGuideWritePlatformService {

	
	public CommandProcessingResult createEpgProgramGuide(JsonCommand command, Long id);
}
