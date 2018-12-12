package org.obsplatform.organisation.ippool.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface IpPoolManagementWritePlatformService {

	CommandProcessingResult createIpPoolManagement(JsonCommand command);

	CommandProcessingResult editIpPoolManagement(JsonCommand command);

	CommandProcessingResult updateIpStatus(Long entityId);

	CommandProcessingResult updateIpDescription(JsonCommand command);

	CommandProcessingResult updateIpAddressStatus(JsonCommand command);

	CommandProcessingResult updateStaticIpStatus(JsonCommand command);
	

}
