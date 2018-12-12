package org.obsplatform.scheduledjobs.dataupload.handler;


import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.scheduledjobs.dataupload.service.DataUploadWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class processDataUploadCommandHandler implements NewCommandSourceHandler{

	private DataUploadWritePlatformService dataUploadWritePlatformService;
	
	
	@Autowired
	public processDataUploadCommandHandler(final DataUploadWritePlatformService dataUploadWritePlatformService){
		
		this.dataUploadWritePlatformService = dataUploadWritePlatformService;
	}
	
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return dataUploadWritePlatformService.processDatauploadFile(command.entityId()) ;
	}

}
