package org.obsplatform.logistics.mrn.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.mrn.service.MRNDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateMRNDetailsMoveCommandHandler implements NewCommandSourceHandler{

	final private MRNDetailsWritePlatformService mrnDetailsWritePlatformService;
	
	@Autowired
	public CreateMRNDetailsMoveCommandHandler(final MRNDetailsWritePlatformService mrnDetailsMoveCommandHandler) {
		this.mrnDetailsWritePlatformService = mrnDetailsMoveCommandHandler;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return mrnDetailsWritePlatformService.moveMRN(command);
	}
}
