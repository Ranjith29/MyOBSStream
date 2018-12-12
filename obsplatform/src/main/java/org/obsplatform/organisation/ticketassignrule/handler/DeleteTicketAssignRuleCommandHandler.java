package org.obsplatform.organisation.ticketassignrule.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.ticketassignrule.service.TicketAssignRuleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteTicketAssignRuleCommandHandler implements NewCommandSourceHandler {

    private final TicketAssignRuleWritePlatformService writePlatformService;
	
	@Autowired
	public DeleteTicketAssignRuleCommandHandler(final TicketAssignRuleWritePlatformService writePlatformService){
		this.writePlatformService = writePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.writePlatformService.deleteTicketAssignRule(command.entityId());
	}

}
