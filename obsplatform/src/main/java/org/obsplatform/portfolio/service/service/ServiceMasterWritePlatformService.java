package org.obsplatform.portfolio.service.service;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ServiceMasterWritePlatformService {

	 CommandProcessingResult updateService(Long serviceId, JsonCommand command);

	 CommandProcessingResult deleteService(Long serviceId);

	 CommandProcessingResult createNewService(JsonCommand command);

}
