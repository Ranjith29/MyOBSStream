package org.obsplatform.billing.currency.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface CountryCurrencyWritePlatformService {

	CommandProcessingResult createCountryCurrency(JsonCommand command);

	CommandProcessingResult updateCountryCurrency(Long entityId,JsonCommand command);

	CommandProcessingResult deleteCountryCurrency(Long entityId);

}
