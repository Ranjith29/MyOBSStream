package org.obsplatform.organisation.feemaster.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface FeeMasterWriteplatformService {

	CommandProcessingResult createFeeMaster(JsonCommand command);
	
	CommandProcessingResult updateFeeMaster(JsonCommand command);
	
	CommandProcessingResult deleteFeeMaster(Long id);
}
