package org.obsplatform.organisation.officepayments.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface OfficePaymentsWritePlatformService {

	CommandProcessingResult createOfficePayment(JsonCommand command);
}
