package org.obsplatform.logistics.agent.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ItemSaleWriteplatformService {

	CommandProcessingResult createNewItemSale(JsonCommand command);

}
