package org.obsplatform.workflow.eventvalidation.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EventValidationWritePlatformService {

	CommandProcessingResult createEventValidation(JsonCommand command);

	CommandProcessingResult deleteEventValidation(Long entityId);

}
