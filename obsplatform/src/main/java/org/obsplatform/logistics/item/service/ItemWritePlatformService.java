package org.obsplatform.logistics.item.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ItemWritePlatformService {

	CommandProcessingResult createItem(JsonCommand command);

	CommandProcessingResult updateItem(JsonCommand command, Long itemId);

	CommandProcessingResult deleteItem(Long itemId);

}
