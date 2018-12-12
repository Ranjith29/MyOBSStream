package org.obsplatform.finance.adjustment.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.adjustment.service.AdjustmentWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateAdjustmentCommandHandler  implements NewCommandSourceHandler{

	  private final AdjustmentWritePlatformService writePlatformService;
		  
	  @Autowired
	    public CreateAdjustmentCommandHandler(final AdjustmentWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	       
	    }
	
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return writePlatformService.createAdjustment(command);
		
	}

}
