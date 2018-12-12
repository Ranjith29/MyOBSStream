package org.obsplatform.billing.promotioncodes.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PromotionCodeWritePlatformService {

	CommandProcessingResult createPromotionCode(JsonCommand command);

	CommandProcessingResult updatePromotionCode(Long id, JsonCommand command);

	CommandProcessingResult deletePromotionCode(Long id);

}
