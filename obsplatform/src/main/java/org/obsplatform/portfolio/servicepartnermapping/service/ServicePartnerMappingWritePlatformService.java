package org.obsplatform.portfolio.servicepartnermapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author Naresh
 * 
 */
public interface ServicePartnerMappingWritePlatformService {

	CommandProcessingResult createServicePartnerMapping(JsonCommand command);

	CommandProcessingResult updateServicePartnerMapping(Long entityId, JsonCommand command);

	CommandProcessingResult deleteServicePartnerMapping(Long entityId);

}
