package org.obsplatform.portfolio.planmapping.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.planmapping.service.PlanMappingWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class UpdateProvisioningPlanMappingCommandHandler implements NewCommandSourceHandler {

private final PlanMappingWritePlatformService writePlatformService;
	
	@Autowired
	public UpdateProvisioningPlanMappingCommandHandler(PlanMappingWritePlatformService writePlatformService){
		this.writePlatformService = writePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.updateProvisioningPlanMapping(command.entityId(),command);
	}

}
