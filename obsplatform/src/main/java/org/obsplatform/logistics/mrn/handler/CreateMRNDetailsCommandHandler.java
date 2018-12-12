package org.obsplatform.logistics.mrn.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.mrn.service.MRNDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateMRNDetailsCommandHandler implements NewCommandSourceHandler{

	
	private final MRNDetailsWritePlatformService mrnDetailsWritePlatformService;
	
	@Autowired
	public CreateMRNDetailsCommandHandler(final MRNDetailsWritePlatformService mrnDetailsWritePlatformService) {
		this.mrnDetailsWritePlatformService = mrnDetailsWritePlatformService;
	}
	
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return mrnDetailsWritePlatformService.createMRNDetails(command);
	}
	
	
}
