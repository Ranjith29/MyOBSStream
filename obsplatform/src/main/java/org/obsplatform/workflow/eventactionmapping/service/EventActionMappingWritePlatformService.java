package org.obsplatform.workflow.eventactionmapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EventActionMappingWritePlatformService {

	CommandProcessingResult createEventActionMapping(JsonCommand command);

	CommandProcessingResult updateEventActionMapping(Long id,
			JsonCommand command);

	CommandProcessingResult deleteEventActionMapping(Long id);

}
