package org.obsplatform.billing.planprice.service;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PriceWritePlatformService {


	CommandProcessingResult createPricing(Long planId,JsonCommand command);

	CommandProcessingResult updatePrice(Long priceId, JsonCommand command);

	CommandProcessingResult deletePrice(Long entityId);

	


}
