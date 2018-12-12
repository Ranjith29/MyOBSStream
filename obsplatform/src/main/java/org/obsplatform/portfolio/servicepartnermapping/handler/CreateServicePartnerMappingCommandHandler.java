package org.obsplatform.portfolio.servicepartnermapping.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.servicepartnermapping.service.ServicePartnerMappingWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Naresh
 * 
 */
@Service
public class CreateServicePartnerMappingCommandHandler implements NewCommandSourceHandler {

	private final ServicePartnerMappingWritePlatformService servicePartnerMappingWritePlatformService;

	@Autowired
	public CreateServicePartnerMappingCommandHandler(final ServicePartnerMappingWritePlatformService servicePartnerMappingWritePlatformService) {
		this.servicePartnerMappingWritePlatformService = servicePartnerMappingWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return servicePartnerMappingWritePlatformService.createServicePartnerMapping(command);
	}

}
