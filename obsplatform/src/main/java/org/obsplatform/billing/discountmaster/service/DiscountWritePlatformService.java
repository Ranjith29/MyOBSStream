package org.obsplatform.billing.discountmaster.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * @author Ranjith
 * 
 */
public interface DiscountWritePlatformService {

	CommandProcessingResult createNewDiscount(JsonCommand command);

	CommandProcessingResult updateDiscount(Long entityId, JsonCommand command);

	CommandProcessingResult deleteDiscount(Long entityId);

}
