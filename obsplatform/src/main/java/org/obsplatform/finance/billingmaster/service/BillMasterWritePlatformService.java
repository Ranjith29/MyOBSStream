package org.obsplatform.finance.billingmaster.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;


public interface BillMasterWritePlatformService {

	CommandProcessingResult createBillMaster(JsonCommand command, Long entityId);

	CommandProcessingResult cancelBillMaster(Long entityId);

	CommandProcessingResult cancelBatchStatement(String batchId);

}