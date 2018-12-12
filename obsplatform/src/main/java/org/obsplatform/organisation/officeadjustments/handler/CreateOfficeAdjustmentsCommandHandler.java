package org.obsplatform.organisation.officeadjustments.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.officeadjustments.service.OfficeAdjustmentsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOfficeAdjustmentsCommandHandler implements 	NewCommandSourceHandler {

	private final OfficeAdjustmentsWritePlatformService writePlatformService;
	  
	  @Autowired
	    public CreateOfficeAdjustmentsCommandHandler(OfficeAdjustmentsWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	       
	    }
	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return this.writePlatformService.createOfficeAdjustment(command);
	}

}
