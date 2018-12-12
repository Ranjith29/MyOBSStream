package org.obsplatform.organisation.groupsdetails.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface GroupsDetailsWritePlatformService {

	CommandProcessingResult createGroupsDetails(JsonCommand command);
	
	CommandProcessingResult createGroupsDetailsProvision(JsonCommand command);
	
	CommandProcessingResult generateStatment(JsonCommand command, Long entityId);
}
