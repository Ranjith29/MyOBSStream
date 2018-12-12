package org.obsplatform.crm.userchat.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.crm.userchat.service.UserChatWriteplatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.plan.service.PlanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserChatMessageCommandHandler implements NewCommandSourceHandler {

    private final UserChatWriteplatformService writePlatformService;

    @Autowired
    public DeleteUserChatMessageCommandHandler(final UserChatWriteplatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.deleteUserChatMessage(command.entityId());
    }
}