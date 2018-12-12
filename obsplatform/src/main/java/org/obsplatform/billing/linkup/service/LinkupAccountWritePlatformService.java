package org.obsplatform.billing.linkup.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface LinkupAccountWritePlatformService {

	public CommandProcessingResult createLinkupAccount(JsonCommand command);

}

