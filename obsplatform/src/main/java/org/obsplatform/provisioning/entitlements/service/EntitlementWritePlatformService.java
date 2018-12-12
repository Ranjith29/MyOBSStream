package org.obsplatform.provisioning.entitlements.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EntitlementWritePlatformService {

	public CommandProcessingResult create(JsonCommand command);
}
