package org.obsplatform.organisation.region.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.region.service.RegionWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UpdateRegionCommandHandler implements NewCommandSourceHandler {

	private RegionWriteplatformService regionWriteplatformService;
	
	@Autowired
	public UpdateRegionCommandHandler(final RegionWriteplatformService regionWriteplatformService) {
		this.regionWriteplatformService = regionWriteplatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.regionWriteplatformService.updateRegion(command);
	}

}

