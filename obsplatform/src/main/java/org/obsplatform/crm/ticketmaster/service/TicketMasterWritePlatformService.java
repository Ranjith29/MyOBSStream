package org.obsplatform.crm.ticketmaster.service;

import java.io.InputStream;

import org.obsplatform.crm.ticketmaster.command.TicketMasterCommand;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.documentmanagement.command.DocumentCommand;

public interface TicketMasterWritePlatformService {

	CommandProcessingResult createTicketMaster(JsonCommand command);
	
	/*
	 * added action param for upload
	 * */
	Long upDateTicketDetails(TicketMasterCommand ticketMasterCommand,DocumentCommand documentCommand, Long ticketId,InputStream inputStream,String ticketURL, String action);

	CommandProcessingResult closeTicket(JsonCommand command);

	String retrieveTicketProblems(Long ticketId);
	
	void processNotifyMessages(String eventName, Long clientId, String orderId, String actionType);


	
}