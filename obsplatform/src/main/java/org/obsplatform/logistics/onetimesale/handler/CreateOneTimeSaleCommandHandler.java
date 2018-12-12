
package org.obsplatform.logistics.onetimesale.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOneTimeSaleCommandHandler implements NewCommandSourceHandler {

    private final OneTimeSaleWritePlatformService writePlatformService;

    @Autowired
    public CreateOneTimeSaleCommandHandler(final OneTimeSaleWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.createOneTimeSale(command,command.entityId());
    }
}