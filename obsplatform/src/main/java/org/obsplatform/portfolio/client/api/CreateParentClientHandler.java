package org.obsplatform.portfolio.client.api;


import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateParentClientHandler implements NewCommandSourceHandler {

	private final ClientWritePlatformService clientWritePlatformService;

    @Autowired
    public CreateParentClientHandler(final ClientWritePlatformService clientWritePlatformService) {
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.clientWritePlatformService.createParentClient(command.entityId(),command);
    }
}