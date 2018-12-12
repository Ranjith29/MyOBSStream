package org.obsplatform.organisation.ippool.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.ippool.service.IpPoolManagementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateIpStatusCommandHandler implements NewCommandSourceHandler {

	private final IpPoolManagementWritePlatformService ipPoolManagementWritePlatformService;

	@Autowired
	public UpdateIpStatusCommandHandler(
			final IpPoolManagementWritePlatformService ipPoolManagementWritePlatformService) {
		this.ipPoolManagementWritePlatformService = ipPoolManagementWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {

		return this.ipPoolManagementWritePlatformService.updateIpStatus(command
				.entityId());
	}
}
