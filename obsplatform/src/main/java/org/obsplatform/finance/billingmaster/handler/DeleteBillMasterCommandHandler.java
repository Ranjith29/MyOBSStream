
package org.obsplatform.finance.billingmaster.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.billingmaster.service.BillMasterWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteBillMasterCommandHandler implements NewCommandSourceHandler {

    private final BillMasterWritePlatformService writePlatformService;

    @Autowired
    public DeleteBillMasterCommandHandler(final BillMasterWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.cancelBillMaster(command.entityId());
    }
}