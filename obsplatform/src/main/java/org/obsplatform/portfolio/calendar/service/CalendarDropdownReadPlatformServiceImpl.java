package org.obsplatform.portfolio.calendar.service;

import java.util.List;

import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.portfolio.calendar.domain.CalendarEntityType;
import org.obsplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.obsplatform.portfolio.calendar.domain.CalendarType;
import org.springframework.stereotype.Service;

@Service
public class CalendarDropdownReadPlatformServiceImpl implements CalendarDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveCalendarEntityTypeOptions() {
        return CalendarEnumerations.calendarEntityType(CalendarEntityType.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarTypeOptions() {
        return CalendarEnumerations.calendarType(CalendarType.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarRemindByOptions() {
        return CalendarEnumerations.calendarRemindBy(CalendarRemindBy.values());
    }

}
