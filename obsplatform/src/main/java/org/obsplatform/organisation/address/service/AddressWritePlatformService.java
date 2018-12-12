package org.obsplatform.organisation.address.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface AddressWritePlatformService {
	
	

	CommandProcessingResult updateAddress(Long clientId, JsonCommand command);

	CommandProcessingResult createLocation(JsonCommand command,String supportedEntityType);

	CommandProcessingResult createAddress(Long clientId, JsonCommand command);
	

	CommandProcessingResult deleteLocation(JsonCommand command,String entityType, Long id);

	CommandProcessingResult updateLocation(JsonCommand command,String supportedEntityType, Long entityId);

	CommandProcessingResult deleteAddress(Long entityId, JsonCommand command);

	

}
