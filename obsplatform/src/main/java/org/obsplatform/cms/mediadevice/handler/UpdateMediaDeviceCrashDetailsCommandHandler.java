package org.obsplatform.cms.mediadevice.handler;

import org.obsplatform.cms.mediadevice.service.MediaDeviceWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateMediaDeviceCrashDetailsCommandHandler implements NewCommandSourceHandler{

	private final MediaDeviceWritePlatformService mediaDeviceWritePlatformService;
	 @Autowired
	    public UpdateMediaDeviceCrashDetailsCommandHandler(final MediaDeviceWritePlatformService mediaDeviceWritePlatformService){
		 this.mediaDeviceWritePlatformService = mediaDeviceWritePlatformService;
	 }
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.mediaDeviceWritePlatformService.updateMediaDetailsCrashStatus(command.entityId(),command);
	}

}
