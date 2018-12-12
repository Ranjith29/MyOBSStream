package org.obsplatform.billing.taxmapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;
@Service
public interface TaxMapWritePlatformService {

	public CommandProcessingResult createTaxMap(final JsonCommand command);
	public CommandProcessingResult updateTaxMap(final JsonCommand command, final Long taxMapId);
}
