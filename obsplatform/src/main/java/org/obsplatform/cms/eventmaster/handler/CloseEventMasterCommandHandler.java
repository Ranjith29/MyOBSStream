/**
 * 
 */
package org.obsplatform.cms.eventmaster.handler;

import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.cms.eventmaster.service.EventMasterWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link Service} Class for closing {@link EventMaster}
 * implements {@link NewCommandSourceHandler}
 * 
 * @author pavani
 *
 */
@Service
public class CloseEventMasterCommandHandler implements NewCommandSourceHandler {

	@Autowired
	private EventMasterWritePlatformService eventMasterWritePlatformService;
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.eventMasterWritePlatformService.deleteEventMaster(command.entityId());
	}

}
