package org.obsplatform.provisioning.provisioning.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ProvisioningServiceParamsWriteplatformService {

	CommandProcessingResult updateServiceParams(JsonCommand command,Long entityId);

}
