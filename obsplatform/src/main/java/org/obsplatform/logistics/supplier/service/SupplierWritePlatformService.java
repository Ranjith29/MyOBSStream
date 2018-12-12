package org.obsplatform.logistics.supplier.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface SupplierWritePlatformService {

	CommandProcessingResult createSupplier(JsonCommand command);

	CommandProcessingResult updateSupplier(JsonCommand command, Long entityId);

}
