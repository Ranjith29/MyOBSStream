package org.obsplatform.logistics.agent.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.agent.service.ItemSaleWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateItemSaleCommandHandler implements NewCommandSourceHandler {
	
    
	private final ItemSaleWriteplatformService agentWriteplatformService;
	
	@Autowired
	public CreateItemSaleCommandHandler(final ItemSaleWriteplatformService agentWriteplatformService) {
		this.agentWriteplatformService=agentWriteplatformService;
	}

	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return this.agentWriteplatformService.createNewItemSale(command);
	}

}
