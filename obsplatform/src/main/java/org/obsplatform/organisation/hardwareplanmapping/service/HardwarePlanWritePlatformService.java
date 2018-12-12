package org.obsplatform.organisation.hardwareplanmapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface HardwarePlanWritePlatformService {

	CommandProcessingResult createHardwarePlan(JsonCommand command);

	CommandProcessingResult updatePlanMapping(Long entityId, JsonCommand command);

}
