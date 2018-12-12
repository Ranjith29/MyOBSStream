package org.obsplatform.portfolio.calendar.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface CalendarWritePlatformService {

    CommandProcessingResult createCalendar(JsonCommand command);

    CommandProcessingResult updateCalendar(JsonCommand command);

    CommandProcessingResult deleteCalendar(Long calendarId);
    
    CommandProcessingResult createCalendarInstance(Long calendarId, Long entityId, Integer entityTypeId);
    
    CommandProcessingResult updateCalendarInstance(Long calendarId, Long entityId, Integer entityTypeId);
}
