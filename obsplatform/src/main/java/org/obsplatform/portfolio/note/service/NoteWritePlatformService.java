package org.obsplatform.portfolio.note.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface NoteWritePlatformService {

    CommandProcessingResult createNote(JsonCommand command);

    CommandProcessingResult updateNote(JsonCommand command);

    CommandProcessingResult deleteNote(JsonCommand command);
}
