package org.obsplatform.logistics.grn.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface GrnDetailsWritePlatformService {


	CommandProcessingResult addGrnDetails(JsonCommand command);

	CommandProcessingResult editGrnDetails(JsonCommand command, Long entityId);
	
}
