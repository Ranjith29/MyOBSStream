package org.obsplatform.logistics.supplier.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.supplier.service.SupplierWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSupplierCommandHandler implements NewCommandSourceHandler {

	
	private SupplierWritePlatformService supplierWritePlatformService;
	
	@Autowired
    public UpdateSupplierCommandHandler(final SupplierWritePlatformService supplierWritePlatformService) {
        this.supplierWritePlatformService = supplierWritePlatformService;
    }
	
	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return this.supplierWritePlatformService.updateSupplier(command,command.entityId());
	}

}

