package org.obsplatform.freeradius.radius.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface RadiusWritePlatformService {

	CommandProcessingResult updateRadService(Long entityId, JsonCommand command);

	CommandProcessingResult deleteRadService(Long entityId);

}
