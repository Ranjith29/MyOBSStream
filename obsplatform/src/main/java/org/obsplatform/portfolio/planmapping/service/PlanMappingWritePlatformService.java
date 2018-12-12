package org.obsplatform.portfolio.planmapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author ashokreddy
 *
 */
public interface PlanMappingWritePlatformService {

	CommandProcessingResult createProvisioningPlanMapping(JsonCommand command);

	CommandProcessingResult updateProvisioningPlanMapping(Long planMapId, JsonCommand command);
	
}
