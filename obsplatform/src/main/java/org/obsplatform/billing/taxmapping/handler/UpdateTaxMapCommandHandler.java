package org.obsplatform.billing.taxmapping.handler;

import org.obsplatform.billing.taxmapping.service.TaxMapWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateTaxMapCommandHandler implements NewCommandSourceHandler{

	private TaxMapWritePlatformService taxMapWritePlatformService;
	
	@Autowired
	public UpdateTaxMapCommandHandler(final TaxMapWritePlatformService taxMapWritePlatformService) {
		this.taxMapWritePlatformService = taxMapWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return taxMapWritePlatformService.updateTaxMap(command, command.entityId());
	}

	
}
