package org.obsplatform.portfolio.association.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDeAssociationCommandhandler implements NewCommandSourceHandler {

	private final HardwareAssociationWriteplatformService writePlatformService;
	  
	  @Autowired
	    public UpdateDeAssociationCommandhandler(HardwareAssociationWriteplatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	       
	    }

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		// TODO Auto-generated method stub
		return this.writePlatformService.deAssociationHardware(command.entityId());
	}
	
}
