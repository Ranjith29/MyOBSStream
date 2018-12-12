package org.obsplatform.logistics.item.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.item.service.ItemWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateItemCommandHandler implements NewCommandSourceHandler {

	
	private ItemWritePlatformService itemWritePlatformService;
	
	@Autowired
    public CreateItemCommandHandler(final ItemWritePlatformService itemWritePlatformService) {
        this.itemWritePlatformService = itemWritePlatformService;
    }
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return this.itemWritePlatformService.createItem(command);
	}

}
