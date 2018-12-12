package org.obsplatform.portfolio.note.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.note.service.NoteWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateNoteCommandHandler implements NewCommandSourceHandler {

    private final NoteWritePlatformService writePlatformService;

    @Autowired
    public UpdateNoteCommandHandler(final NoteWritePlatformService noteWritePlatformService) {
        this.writePlatformService = noteWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.updateNote(command);
    }

}
