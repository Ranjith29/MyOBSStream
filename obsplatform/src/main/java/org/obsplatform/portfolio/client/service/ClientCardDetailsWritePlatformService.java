package org.obsplatform.portfolio.client.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ClientCardDetailsWritePlatformService {

	CommandProcessingResult addClientCardDetails(Long clientId,JsonCommand command);

	CommandProcessingResult updateClientCardDetails(JsonCommand command);

	CommandProcessingResult deleteClientCardDetails(JsonCommand command);
	
	

}
