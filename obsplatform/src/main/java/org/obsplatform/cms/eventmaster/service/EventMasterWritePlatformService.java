/**
 * 
 */
package org.obsplatform.cms.eventmaster.service;

import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Interface for {@link EventMaster} Write Servcie
 * 
 * @author pavani
 * @author Rakesh
 */
public interface EventMasterWritePlatformService {

	
	/**
	 * Method to Create {@link EventMaster}
	 * 
	 * @param command
	 * @return
	 */
	CommandProcessingResult createEventMaster (JsonCommand command); 
	
	/**
	 * Method to Update {@link EventMaster}
	 * 
	 * @param command
	 * @return
	 */
	CommandProcessingResult updateEventMaster(JsonCommand command);
	
	/**
	 * Method to Delete {@link EventMaster}
	 * 
	 * @param eventId
	 * @return
	 */
	CommandProcessingResult deleteEventMaster(Long eventId);
}
