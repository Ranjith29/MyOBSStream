package org.obsplatform.organisation.ticketassignrule.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface TicketAssignRuleWritePlatformService {

	CommandProcessingResult createTicketAssignRule(JsonCommand command);
	
	CommandProcessingResult updateTicketAssignRule(Long entityId,JsonCommand command);
	
	CommandProcessingResult deleteTicketAssignRule(Long entityId);

}
