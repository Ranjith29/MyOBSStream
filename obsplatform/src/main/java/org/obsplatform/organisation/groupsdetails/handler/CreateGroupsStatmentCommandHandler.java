package org.obsplatform.organisation.groupsdetails.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.groupsdetails.service.GroupsDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateGroupsStatmentCommandHandler implements NewCommandSourceHandler{
	
	private final GroupsDetailsWritePlatformService groupsDetailsWritePlatformService;
	
	@Autowired
	public CreateGroupsStatmentCommandHandler(final GroupsDetailsWritePlatformService groupsDetailsWritePlatformService){
		
		this.groupsDetailsWritePlatformService = groupsDetailsWritePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return this.groupsDetailsWritePlatformService.generateStatment(command,command.entityId());
	}

}
