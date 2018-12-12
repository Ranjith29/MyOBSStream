package org.obsplatform.organisation.department.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface DepartmentWritePlatformService {

	CommandProcessingResult createDepartment(JsonCommand command);
	
	CommandProcessingResult updateDepartment(Long entityId,JsonCommand command);
	
	CommandProcessingResult deleteDepartment(Long entityId);
}
