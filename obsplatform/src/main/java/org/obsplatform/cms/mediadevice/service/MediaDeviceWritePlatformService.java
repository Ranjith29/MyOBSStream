package org.obsplatform.cms.mediadevice.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface MediaDeviceWritePlatformService {

	CommandProcessingResult updateMediaDetailsStatus(JsonCommand command);

	CommandProcessingResult updateMediaDetailsCrashStatus(Long entityId,JsonCommand command);
	
	
}
