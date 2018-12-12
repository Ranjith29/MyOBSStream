package org.obsplatform.portfolio.servicepartnermapping.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.servicepartnermapping.service.ServicePartnerMappingWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteServicePartnerMappingCommandHandler implements NewCommandSourceHandler  {

	 private final ServicePartnerMappingWritePlatformService servicePartnerMappingWritePlatformService;

	    @Autowired
	    public DeleteServicePartnerMappingCommandHandler(final ServicePartnerMappingWritePlatformService servicePartnerMappingWritePlatformService) {
	        this.servicePartnerMappingWritePlatformService = servicePartnerMappingWritePlatformService;
	    }

		@Override
		public CommandProcessingResult processCommand(JsonCommand command) {
			return this.servicePartnerMappingWritePlatformService.deleteServicePartnerMapping(command.entityId());
		}
}
