package org.obsplatform.crm.ticketmaster.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.crm.ticketmaster.service.TicketMasterWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nookala
 *
 */
@Service
public class CreateTicketMasterCommandHandler implements NewCommandSourceHandler {
	
	private final TicketMasterWritePlatformService ticketMasterWriteService;
	
	@Autowired
	public CreateTicketMasterCommandHandler (final TicketMasterWritePlatformService ticketMasterWriteService) {
		this.ticketMasterWriteService = ticketMasterWriteService;
	}
	
	@Transactional
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.ticketMasterWriteService.createTicketMaster(command);
    }
}
