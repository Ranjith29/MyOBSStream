package org.obsplatform.scheduledjobs.dataupload.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.scheduledjobs.dataupload.service.DataUploadWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddUploadStatusCommandHandler implements NewCommandSourceHandler {
	
	private final DataUploadWritePlatformService uploadStatusWritePlatformService;
	
	
	@Autowired
	public AddUploadStatusCommandHandler(final DataUploadWritePlatformService uploadStatusWritePlatformService) {
		this.uploadStatusWritePlatformService = uploadStatusWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		//return this.inventoryItemDetailsWritePlatformService.addItem(command);
		return null;
	}

}
