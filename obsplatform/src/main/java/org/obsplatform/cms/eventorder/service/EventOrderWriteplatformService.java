package org.obsplatform.cms.eventorder.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface EventOrderWriteplatformService {

	CommandProcessingResult createEventOrder(JsonCommand command);

	CommandProcessingResult updateEventOrderPrice(JsonCommand command);

	boolean checkClientBalance(Double bookedPrice, Long clientId,boolean isWalletEnable);

}
