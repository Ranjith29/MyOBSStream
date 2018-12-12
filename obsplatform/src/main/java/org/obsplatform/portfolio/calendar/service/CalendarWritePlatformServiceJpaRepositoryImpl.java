/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.calendar.service;

import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.portfolio.calendar.domain.Calendar;
import org.obsplatform.portfolio.calendar.domain.CalendarInstance;
import org.obsplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.obsplatform.portfolio.calendar.domain.CalendarRepository;
import org.obsplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.obsplatform.portfolio.calendar.serialization.CalendarCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarWritePlatformServiceJpaRepositoryImpl implements CalendarWritePlatformService {

    private final CalendarRepository calendarRepository;
    private final CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CalendarInstanceRepository calendarInstanceRepository;

    @Autowired
    public CalendarWritePlatformServiceJpaRepositoryImpl(final CalendarRepository calendarRepository,
            final CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CalendarInstanceRepository calendarInstanceRepository) {
        this.calendarRepository = calendarRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.calendarInstanceRepository = calendarInstanceRepository;
    }

    @Override
    public CommandProcessingResult createCalendar(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateForCreate(command.json());

        final Calendar newCalendar = Calendar.fromJson(command);
        this.calendarRepository.save(newCalendar);

        final CalendarInstance newCalendarInstance = CalendarInstance.fromJson(newCalendar, command);
        this.calendarInstanceRepository.save(newCalendarInstance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newCalendar.getId()) //
                .build();

    }

    @Override
    public CommandProcessingResult updateCalendar(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final Long calendarId = command.entityId();
        final Calendar calendarForUpdate = this.calendarRepository.findOne(calendarId);
        if (calendarForUpdate == null) { throw new CalendarNotFoundException(calendarId); }

        final Map<String, Object> changes = calendarForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.calendarRepository.saveAndFlush(calendarForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(calendarForUpdate.getId()) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteCalendar(final Long calendarId) {
        final Calendar calendarForDelete = this.calendarRepository.findOne(calendarId);
        if (calendarForDelete == null) { throw new CalendarNotFoundException(calendarId); }

        this.calendarRepository.delete(calendarForDelete);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(calendarId) //
                .build();
    }

    @Override
    public CommandProcessingResult createCalendarInstance(final Long calendarId, final Long entityId, final Integer entityTypeId) {

        final Calendar calendarForUpdate = this.calendarRepository.findOne(calendarId);
        if (calendarForUpdate == null) { throw new CalendarNotFoundException(calendarId); }
        
        final CalendarInstance newCalendarInstance = new CalendarInstance(calendarForUpdate, entityId, entityTypeId);
        this.calendarInstanceRepository.save(newCalendarInstance);
        
        return new CommandProcessingResultBuilder() //
        .withCommandId(null) //
        .withEntityId(calendarForUpdate.getId()) //
        .build();
    }

    @Override
    public CommandProcessingResult updateCalendarInstance(Long calendarId, Long entityId, Integer entityTypeId) {
        final Calendar calendarForUpdate = this.calendarRepository.findOne(calendarId);
        if (calendarForUpdate == null) { throw new CalendarNotFoundException(calendarId); }
        
        final CalendarInstance calendarInstanceForUpdate = this.calendarInstanceRepository.findByCalendarAndEntityIdAndEntityTypeId(calendarId, entityId, entityTypeId);
        this.calendarInstanceRepository.saveAndFlush(calendarInstanceForUpdate);
        return new CommandProcessingResultBuilder() //
        .withCommandId(null) //
        .withEntityId(calendarForUpdate.getId()) //
        .build();
    }

    
}
