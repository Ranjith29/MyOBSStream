package org.obsplatform.portfolio.contract.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.contract.service.ContractPeriodWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteContractCommandHandler implements NewCommandSourceHandler {

    private final ContractPeriodWritePlatformService writePlatformService;

    @Autowired
    public DeleteContractCommandHandler(final ContractPeriodWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.deleteContract(command.entityId());
    }
}