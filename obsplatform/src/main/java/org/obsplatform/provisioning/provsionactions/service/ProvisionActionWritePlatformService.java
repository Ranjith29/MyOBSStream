package org.obsplatform.provisioning.provsionactions.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ProvisionActionWritePlatformService {

	CommandProcessingResult updateProvisionActionStatus(JsonCommand command);

}
