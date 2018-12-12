package org.obsplatform.portfolio.servicemapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ServiceMappingWritePlatformService {

	CommandProcessingResult createServiceMapping(JsonCommand command);

	CommandProcessingResult updateServiceMapping(Long entityId,JsonCommand command);

}
