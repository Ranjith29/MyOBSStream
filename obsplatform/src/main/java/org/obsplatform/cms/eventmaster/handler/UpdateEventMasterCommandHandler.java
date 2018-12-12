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
 * {@link Service} Class for updating {@link EventMaster}
 * implements {@link NewCommandSourceHandler}
 * 
 * @author pavani
 * @author Rakesh
 */
@Service
public class UpdateEventMasterCommandHandler implements NewCommandSourceHandler {

	@Autowired
	private EventMasterWritePlatformService eventMasterWritePlatformService;

	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return this.eventMasterWritePlatformService.updateEventMaster(command);
	}

}
