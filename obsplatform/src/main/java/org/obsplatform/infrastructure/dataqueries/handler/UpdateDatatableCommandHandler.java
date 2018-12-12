package org.obsplatform.infrastructure.dataqueries.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDatatableCommandHandler implements NewCommandSourceHandler {

    private final ReadWriteNonCoreDataService writePlatformService;

    @Autowired
    public UpdateDatatableCommandHandler(final ReadWriteNonCoreDataService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        final String datatableName = command.getUrl().replaceAll("/datatables/", "");

        this.writePlatformService.updateDatatable(datatableName, command);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(datatableName).build();
    }
}
