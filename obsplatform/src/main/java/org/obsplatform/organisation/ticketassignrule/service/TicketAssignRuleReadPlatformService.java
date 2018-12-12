package org.obsplatform.organisation.ticketassignrule.service;

import java.util.List;

import org.obsplatform.organisation.ticketassignrule.data.TicketAssignRuleData;

public interface TicketAssignRuleReadPlatformService {

	TicketAssignRuleData retrieveCategoryDepartment(Long businessprocessId, Long clientCategoryId); 
	
	TicketAssignRuleData retrieveTicketAssignRuleData(Long deptId);
	
	List<TicketAssignRuleData> retrieveAllTicketAssignRuleData();
}
