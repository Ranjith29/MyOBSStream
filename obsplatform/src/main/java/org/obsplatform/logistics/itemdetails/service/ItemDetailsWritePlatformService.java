package org.obsplatform.logistics.itemdetails.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsAllocation;

public interface ItemDetailsWritePlatformService {


	CommandProcessingResult addItem(JsonCommand json,Long orderId);
	
	CommandProcessingResult allocateHardware(JsonCommand command);
	
	ItemDetailsAllocation deAllocateHardware(String serialNo,Long clientId,String entityName);
	
	CommandProcessingResult updateItem(Long itemId,JsonCommand json);
	
	CommandProcessingResult deAllocateHardware(JsonCommand command);

	CommandProcessingResult deleteItem(Long itemId, JsonCommand command);
}
