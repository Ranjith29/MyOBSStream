package org.obsplatform.organisation.partner.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.partner.service.PartnersAgreementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePartnerAgreementCommandHandler implements NewCommandSourceHandler {

	private final PartnersAgreementWritePlatformService writePlatformService;

	@Autowired
	public DeletePartnerAgreementCommandHandler(final PartnersAgreementWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.deletePartnerAgreement(command.entityId());
	}
}