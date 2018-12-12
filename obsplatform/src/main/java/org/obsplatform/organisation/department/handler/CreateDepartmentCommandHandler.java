/**
	 * This Source Code Form is subject to the terms of the Mozilla Public
	 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
	 * You can obtain one at http://mozilla.org/MPL/2.0/.
	 */

package org.obsplatform.organisation.department.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.department.service.DepartmentWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateDepartmentCommandHandler implements NewCommandSourceHandler{

	private final DepartmentWritePlatformService writePlatformService;
	
	@Autowired
    public CreateDepartmentCommandHandler(final DepartmentWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return this.writePlatformService.createDepartment(command);
	}
}
