package org.obsplatform.portfolio.isexdirectory.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author Naresh
 *
 */
public interface IsExDirectoryWritePlatformService {
	
	CommandProcessingResult createIsExDirectory(JsonCommand command);

	CommandProcessingResult updateIsExDirectory(Long entityId, JsonCommand command);
	
}
