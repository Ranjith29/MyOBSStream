package org.obsplatform.organisation.officepayments.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.officepayments.service.OfficePaymentsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOfficePaymentsCommandHandler implements NewCommandSourceHandler {

	private final OfficePaymentsWritePlatformService writePlatformService;
	
	@Autowired
	public CreateOfficePaymentsCommandHandler(OfficePaymentsWritePlatformService writePlatformService){
		this.writePlatformService = writePlatformService;
	}
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return this.writePlatformService.createOfficePayment(command);
	}

}
