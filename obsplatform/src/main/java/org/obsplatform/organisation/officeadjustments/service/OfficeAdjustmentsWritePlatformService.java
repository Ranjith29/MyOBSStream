package org.obsplatform.organisation.officeadjustments.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface OfficeAdjustmentsWritePlatformService {
	
	CommandProcessingResult createOfficeAdjustment(JsonCommand command);
}
