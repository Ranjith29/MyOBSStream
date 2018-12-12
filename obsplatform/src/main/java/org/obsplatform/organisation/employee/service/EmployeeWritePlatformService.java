package org.obsplatform.organisation.employee.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EmployeeWritePlatformService {
	
	CommandProcessingResult createEmployee(JsonCommand command);
	
	 CommandProcessingResult updateEmployee(JsonCommand command, Long employeeId);
	 
	 CommandProcessingResult deleteEmployee(JsonCommand command, Long employeeId);
	 

}
