package org.obsplatform.logistics.supplier.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.supplier.service.SupplierWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateSupplierCommandHandler implements NewCommandSourceHandler{

	final private SupplierWritePlatformService supplierWritePlatformService;
	
	@Autowired
	public CreateSupplierCommandHandler(final SupplierWritePlatformService supplierWritePlatformService) {
		this.supplierWritePlatformService = supplierWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return supplierWritePlatformService.createSupplier(command);
	}
	
	
}
