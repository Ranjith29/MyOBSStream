package org.obsplatform.organisation.voucher.handler;


import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.voucher.service.VoucherWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 *
 */

@Service
public class CreateVoucherGroupCommandHandler implements NewCommandSourceHandler  {

	private final VoucherWritePlatformService writePlatformService;

    @Autowired
    public CreateVoucherGroupCommandHandler(final VoucherWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
    	return this.writePlatformService.createRandomGenerator(command);
	}
	
}
