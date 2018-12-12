package org.obsplatform.logistics.mrn.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface MRNDetailsWritePlatformService {

	CommandProcessingResult createMRNDetails(JsonCommand command);

	CommandProcessingResult moveMRN(JsonCommand command);

	CommandProcessingResult moveItemSale(JsonCommand command);

}
