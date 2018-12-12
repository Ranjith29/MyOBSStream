package org.obsplatform.organisation.redemption.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Defining RedemptionWritePlatformService interface
 */
public interface RedemptionWritePlatformService {
	
	/**
	 * Defining createRedemption abstract method with parameter as JsonCommand
	 */
	CommandProcessingResult createRedemption(JsonCommand command);
}
