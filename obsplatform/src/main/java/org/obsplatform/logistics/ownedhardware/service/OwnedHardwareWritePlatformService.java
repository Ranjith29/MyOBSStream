package org.obsplatform.logistics.ownedhardware.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;


public interface OwnedHardwareWritePlatformService {

	
	public CommandProcessingResult createOwnedHardware(JsonCommand command, Long clientId);

	public CommandProcessingResult updateOwnedHardware(JsonCommand command,Long entityId);
	
	public CommandProcessingResult deleteOwnedHardware(Long entityId);
}
