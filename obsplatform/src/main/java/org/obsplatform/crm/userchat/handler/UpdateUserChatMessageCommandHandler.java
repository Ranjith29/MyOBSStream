package org.obsplatform.crm.userchat.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.crm.userchat.service.UserChatWriteplatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserChatMessageCommandHandler implements NewCommandSourceHandler {

	 private final UserChatWriteplatformService writePlatformService;

	    @Autowired
	    public UpdateUserChatMessageCommandHandler(final UserChatWriteplatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	    }

		@Override
		public CommandProcessingResult processCommand(JsonCommand command) {
	       return this.writePlatformService.updateUserChatMessage(command,command.entityId());
		}
}
