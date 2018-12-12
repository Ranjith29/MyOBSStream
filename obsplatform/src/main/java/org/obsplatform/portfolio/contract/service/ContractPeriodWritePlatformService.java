package org.obsplatform.portfolio.contract.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ContractPeriodWritePlatformService {

	CommandProcessingResult createContract(JsonCommand command);

	CommandProcessingResult updateContract(Long contractId,JsonCommand command);

	CommandProcessingResult deleteContract(Long entityId);

}
