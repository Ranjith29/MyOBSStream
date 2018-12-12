package org.obsplatform.finance.adjustment.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;


public interface AdjustmentWritePlatformService {
	

	CommandProcessingResult createAdjustment(JsonCommand command);
	
}
