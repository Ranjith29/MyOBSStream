package org.obsplatform.crm.ticketmaster.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.crm.ticketmaster.data.ClientTicketData;
import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.crm.ticketmaster.data.UsersData;
import org.obsplatform.crm.ticketmaster.domain.TicketDetail;
import org.obsplatform.crm.ticketmaster.domain.TicketHistory;
import org.obsplatform.crm.ticketmaster.domain.TicketMaster;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;

public interface TicketMasterReadPlatformService {

	List<UsersData> retrieveUsers();

	List<TicketMasterData> retrieveClientTicketDetails(Long clientId);

	TicketMasterData retrieveSingleTicketDetails(Long clientId, Long ticketId);

	List<EnumOptionData> retrievePriorityData();

	List<TicketMasterData> retrieveClientTicketHistory(Long ticketId, String historyParam);

	TicketMasterData retrieveTicket(Long clientId, Long ticketId);
	
	Page<ClientTicketData> retrieveAssignedTicketsForNewClient(SearchSqlQuery searchTicketMaster, String statusType, String assignTo);

	TicketDetail retrieveTicketDetail(Long ticketId);
	
	TicketHistory retrieveTickethistory(Long ticketId);
	
	Object retrieveIssueIdOfLastTicket();
	
	List<TicketMasterData> retrieveTicketsByDate(String statusType,String date);
	
	
}