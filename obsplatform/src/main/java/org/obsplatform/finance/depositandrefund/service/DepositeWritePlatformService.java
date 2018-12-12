package org.obsplatform.finance.depositandrefund.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface DepositeWritePlatformService {


	CommandProcessingResult createDeposite(JsonCommand command);
	
	CommandProcessingResult createRegistrationFee(JsonCommand command);
	
	CommandProcessingResult createSetupFee(JsonCommand command);


}
