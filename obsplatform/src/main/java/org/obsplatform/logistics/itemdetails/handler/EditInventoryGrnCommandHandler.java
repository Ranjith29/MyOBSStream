package org.obsplatform.logistics.itemdetails.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.grn.service.GrnDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EditInventoryGrnCommandHandler implements NewCommandSourceHandler{

	private GrnDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	
	
	@Autowired
	public EditInventoryGrnCommandHandler(final GrnDetailsWritePlatformService inventoryItemDetailsWritePlatformService){
		this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
	}
	
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return inventoryItemDetailsWritePlatformService.editGrnDetails(command,command.entityId());
	}

}
