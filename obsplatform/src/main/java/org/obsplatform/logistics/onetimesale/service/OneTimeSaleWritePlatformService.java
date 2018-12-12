package org.obsplatform.logistics.onetimesale.service;

import java.math.BigDecimal;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.api.JsonQuery;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.item.data.ItemData;

public interface OneTimeSaleWritePlatformService {

	CommandProcessingResult createOneTimeSale(JsonCommand command,Long clientId);

	ItemData calculatePrice(Long itemId, JsonQuery query,Long clientId);

	CommandProcessingResult deleteOneTimeSale(Long entityId);

	BigDecimal calculateChargeVariantPriceForItems(BigDecimal itemprice,
			Long clientId, ItemData itemData);

}
