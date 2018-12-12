package org.obsplatform.portfolio.addons.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface AddOnsWritePlatformService {

	CommandProcessingResult createAddons(JsonCommand command);

	CommandProcessingResult UpdateAddons(JsonCommand command, Long entityId);

	CommandProcessingResult deleteAddons(Long entityId);

}
