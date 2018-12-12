/**
 * 
 */
package org.obsplatform.cms.eventprice.handler;

import org.obsplatform.cms.eventprice.service.EventPriceWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link Service} Class for updating {@link EventPricing} Handler
 * implements {@link NewCommandSourceHandler}
 * 
 * @author pavani
 *
 */
@Service
public class UpdateEventPriceCommandHandler implements NewCommandSourceHandler {

	@Autowired
	private EventPriceWritePlatformService eventPricingWritePlatformService;
	
	
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return this.eventPricingWritePlatformService.updateEventPrice(command);
	}

}
