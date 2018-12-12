package org.obsplatform.billing.chargevariant.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ChargeVariantWritePlatformService {

	CommandProcessingResult createChargeVariant(JsonCommand command);

	CommandProcessingResult updateChargeVariant(Long entityId, JsonCommand command);

	CommandProcessingResult deleteChargeVariant(Long entityId);

}
