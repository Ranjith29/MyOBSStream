package org.obsplatform.portfolio.hardwareswapping.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface HardwareSwappingWriteplatformService {

	CommandProcessingResult doHardWareSwapping(Long entityId,JsonCommand command);

}
