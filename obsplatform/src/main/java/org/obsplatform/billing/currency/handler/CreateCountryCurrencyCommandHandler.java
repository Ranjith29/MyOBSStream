package org.obsplatform.billing.currency.handler;

import org.obsplatform.billing.currency.service.CountryCurrencyWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCountryCurrencyCommandHandler implements
		NewCommandSourceHandler {

	CountryCurrencyWritePlatformService countryCurrencyWritePlatformService;

	@Autowired
	public CreateCountryCurrencyCommandHandler(final CountryCurrencyWritePlatformService countryCurrencyWritePlatformService) {
		this.countryCurrencyWritePlatformService = countryCurrencyWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		return countryCurrencyWritePlatformService.createCountryCurrency(command);
	}

}
