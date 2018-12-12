package org.obsplatform.portfolio.order.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface OrderAddOnsWritePlatformService {

	CommandProcessingResult createOrderAddons(JsonCommand command, Long entityId);

	CommandProcessingResult disconnectOrderAddon(JsonCommand command,Long entityId);

	CommandProcessingResult cancelOrderAddon(JsonCommand command, Long entityId);

	CommandProcessingResult deleteOrderAddon(JsonCommand command, Long entityId);

	

}
