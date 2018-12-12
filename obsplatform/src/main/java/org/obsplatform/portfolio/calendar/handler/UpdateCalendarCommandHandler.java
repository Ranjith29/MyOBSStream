package org.obsplatform.portfolio.calendar.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.calendar.service.CalendarWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCalendarCommandHandler implements NewCommandSourceHandler {

    private final CalendarWritePlatformService calendarWritePlatformService;

    @Autowired
    public UpdateCalendarCommandHandler(final CalendarWritePlatformService calendarWritePlatformService) {
        this.calendarWritePlatformService = calendarWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.calendarWritePlatformService.updateCalendar(command);
    }

}
