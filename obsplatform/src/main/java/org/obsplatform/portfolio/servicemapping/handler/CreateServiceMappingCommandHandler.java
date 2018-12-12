package org.obsplatform.portfolio.servicemapping.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.servicemapping.service.ServiceMappingWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateServiceMappingCommandHandler implements NewCommandSourceHandler{
	
	private final ServiceMappingWritePlatformService serviceMappingWritePlatformService;
	
	@Autowired
	public CreateServiceMappingCommandHandler(final ServiceMappingWritePlatformService serviceMappingWritePlatformService) {
		this.serviceMappingWritePlatformService = serviceMappingWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return serviceMappingWritePlatformService.createServiceMapping(command);
	}

}
