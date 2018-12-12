package org.obsplatform.organisation.department.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.department.service.DepartmentWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDepartmentCommandHandler implements NewCommandSourceHandler {

    private final DepartmentWritePlatformService writePlatformService;
	
	@Autowired
    public UpdateDepartmentCommandHandler(final DepartmentWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.updateDepartment(command.entityId(),command);
	}

}
