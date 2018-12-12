package org.obsplatform.organisation.partner.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.partner.service.PartnersWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

	@Service
	public class CreatePartnerCommandHandler implements NewCommandSourceHandler {

	    private final PartnersWritePlatformService writePlatformService;

	    @Autowired
	    public CreatePartnerCommandHandler(final PartnersWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	    }

	    @Transactional
	    @Override
	    public CommandProcessingResult processCommand(final JsonCommand command) {

	        return this.writePlatformService.createNewPartner(command);
	    }
	}


