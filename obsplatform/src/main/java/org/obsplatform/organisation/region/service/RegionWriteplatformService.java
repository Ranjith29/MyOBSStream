package org.obsplatform.organisation.region.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface RegionWriteplatformService {

	CommandProcessingResult createNewRegion(JsonCommand command);

	CommandProcessingResult updateRegion(JsonCommand command);

	CommandProcessingResult deleteRegion(Long entityId);



}
