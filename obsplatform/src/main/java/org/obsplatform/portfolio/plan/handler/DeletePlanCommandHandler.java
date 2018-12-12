package org.obsplatform.portfolio.plan.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.plan.service.PlanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePlanCommandHandler implements NewCommandSourceHandler {

    private final PlanWritePlatformService writePlatformService;

    @Autowired
    public DeletePlanCommandHandler(final PlanWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.deleteplan(command.entityId());
    }
}