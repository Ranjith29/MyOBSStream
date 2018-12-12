/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DateUtils {

    @SuppressWarnings("deprecation")
	public static Date getDateOfTenant() {
        return getLocalDateOfTenant().toDateMidnight().toDate();
    }

    public static LocalDate getLocalDateOfTenant() {

        LocalDate today = new LocalDate();

        final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        if (tenant != null) {
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            if (zone != null) {
                today = new LocalDate(zone);
            }
        }

        return today;
    }
    
   
    public static LocalDateTime getLocalDateTimeOfTenant() {

        LocalDateTime today = new LocalDateTime();

        final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        if (tenant != null) {
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            if (zone != null) {
                today = new LocalDateTime(zone);
            }
        }

        return today;//Print 2015-08-12T13:32:19.670
    }
    
    public static DateTime getDateTimeOfTenant() {

    	DateTime today = new DateTime();

        final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        if (tenant != null) {
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            if (zone != null) {
            	
                today = new DateTime(zone);

            }
        }

        return today;//Print 2016-08-12T13:32:19.675+02:00
    }
    
    public static String formatToSqlDate(final Date date) {
    	
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String formattedSqlDate = df.format(date);
        return formattedSqlDate;
    }

    public static LocalDate getLocalDateOfTenantForClient() {
    	return getLocalDateOfTenant();
    }

    public static LocalDate parseLocalDate(final String stringDate, final String pattern) {

        try {
        	
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dateStringFormat.parseDateTime(stringDate);
            return dateTime.toLocalDate();
            
        } catch (IllegalArgumentException e) {
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.pattern", "The parameter date ("
                    + stringDate + ") is invalid w.r.t. pattern " + pattern, "date", stringDate, pattern);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public static DateTime parseLocalDateTime(final String stringDate, final String pattern) {

        try {
        	
            DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(pattern);
            DateTime dateTime = dateStringFormat.parseDateTime(stringDate);
            return dateTime;
            
        } catch (IllegalArgumentException e) {
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.pattern", "The parameter date ("
                    + stringDate + ") is invalid w.r.t. pattern " + pattern, "date", stringDate, pattern);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}