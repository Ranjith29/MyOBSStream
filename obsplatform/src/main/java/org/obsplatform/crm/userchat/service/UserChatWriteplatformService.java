package org.obsplatform.crm.userchat.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface UserChatWriteplatformService {

	CommandProcessingResult createUserChat(JsonCommand command);
	
	CommandProcessingResult updateUserChatMessage(JsonCommand command,Long entityId);
	
	CommandProcessingResult deleteUserChatMessage(Long entityId);

}
