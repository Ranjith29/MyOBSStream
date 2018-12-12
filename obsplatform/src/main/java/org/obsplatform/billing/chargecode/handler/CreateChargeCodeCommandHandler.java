package org.obsplatform.billing.chargecode.handler;

import org.obsplatform.billing.chargecode.service.ChargeCodeWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateChargeCodeCommandHandler implements NewCommandSourceHandler {

	private final ChargeCodeWritePlatformService chargeCodeWritePlatformService;

	@Autowired
	public CreateChargeCodeCommandHandler(
			final ChargeCodeWritePlatformService chargeCodeWritePlatformService) {
		this.chargeCodeWritePlatformService = chargeCodeWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return chargeCodeWritePlatformService.createChargeCode(command);
	}

}
