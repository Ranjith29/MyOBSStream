package org.obsplatform.cms.eventprice.handler;

import org.obsplatform.cms.eventprice.service.EventPriceWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Service} Class for Creating {@link EventPricing} handler
 * implements {@link NewCommandSourceHandler}
 * 
 * @author pavani
 *
 */
@Service
public class CreateEventPriceCommandHandler implements NewCommandSourceHandler {

	@Autowired
	private EventPriceWritePlatformService eventPricingWritePlatformService;
	
	
    @Transactional
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.eventPricingWritePlatformService.createEventPrice(command);
    }

}
