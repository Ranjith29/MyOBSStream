package org.obsplatform.organisation.employee.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.employee.service.EmployeeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeeCommandHandler implements NewCommandSourceHandler{

	private final EmployeeWritePlatformService writePlatformService;
	
	
	@Autowired
    public UpdateEmployeeCommandHandler(final EmployeeWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.updateEmployee(command,command.entityId());
	}

	
	
}
