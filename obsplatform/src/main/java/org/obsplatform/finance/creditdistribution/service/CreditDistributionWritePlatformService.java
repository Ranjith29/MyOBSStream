package org.obsplatform.finance.creditdistribution.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;


public interface  CreditDistributionWritePlatformService {

	CommandProcessingResult createCreditDistribution(JsonCommand command);
	
	CommandProcessingResult cancelCreditDistribution(JsonCommand command,Long entityId);



}
