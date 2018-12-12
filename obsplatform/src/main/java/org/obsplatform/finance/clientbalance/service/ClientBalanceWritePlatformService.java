package org.obsplatform.finance.clientbalance.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ClientBalanceWritePlatformService {

	CommandProcessingResult addClientBalance(JsonCommand command);

}
