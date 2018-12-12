/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.InvalidJsonException;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.portfolio.client.api.ClientApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer of JSON for office API.
 */
@Component
public final class OfficeCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "parentId", "openingDate", "externalId",
            "locale", "dateFormat", "officeType","officeNumber","phoneNumber","email","city","state","country"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public OfficeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final LocalDate openingDate = fromApiJsonHelper.extractLocalDateNamed("openingDate", element);
        baseDataValidator.reset().parameter("openingDate").value(openingDate).notNull();

        if (fromApiJsonHelper.parameterExists("externalId", element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
            baseDataValidator.reset().parameter("externalId").value(externalId).notExceedingLengthOf(100);
        }
        
        final Long officeType = fromApiJsonHelper.extractLongNamed("officeType", element);
        baseDataValidator.reset().parameter("officeType").value(officeType).notNull();

        if (fromApiJsonHelper.parameterExists("parentId", element)) {
            final Long parentId = fromApiJsonHelper.extractLongNamed("parentId", element);
            baseDataValidator.reset().parameter("parentId").value(parentId).notNull().integerGreaterThanZero();
        }
        
        final String email = fromApiJsonHelper.extractStringNamed("email", element);
        baseDataValidator.reset().parameter("email").value(email).notBlank();
        
        if(email!=null){
        	final Boolean isValid = email.matches(ClientApiConstants.EMAIL_REGEX);
        	if(!isValid)
        	dataValidationErrors.add(ApiParameterError.parameterError("error.invalid.email.address","Invalid Email Address", "email",email));
        
        } 
        
		final String city = fromApiJsonHelper.extractStringNamed("city", element);
		baseDataValidator.reset().parameter("city").value(city).notBlank().notExceedingLengthOf(100);
		
		final String state = fromApiJsonHelper.extractStringNamed("state", element);
		baseDataValidator.reset().parameter("state").value(state).notBlank().notExceedingLengthOf(100);
		
		final String country = fromApiJsonHelper.extractStringNamed("country", element);
		baseDataValidator.reset().parameter("country").value(country).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");

        final JsonElement element = fromApiJsonHelper.parse(json);

        if (fromApiJsonHelper.parameterExists("name", element)) {
            final String name = fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        final Long officeType = fromApiJsonHelper.extractLongNamed("officeType", element);
        baseDataValidator.reset().parameter("officeType").value(officeType).notNull();

        
        if (fromApiJsonHelper.parameterExists("openingDate", element)) {
            final LocalDate openingDate = fromApiJsonHelper.extractLocalDateNamed("openingDate", element);
            baseDataValidator.reset().parameter("openingDate").value(openingDate).notNull();
        }

        if (fromApiJsonHelper.parameterExists("externalId", element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
            baseDataValidator.reset().parameter("externalId").value(externalId).notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("parentId", element)) {
            final Long parentId = fromApiJsonHelper.extractLongNamed("parentId", element);
            baseDataValidator.reset().parameter("parentId").value(parentId).notNull().integerGreaterThanZero();
        }
        
        final String email = fromApiJsonHelper.extractStringNamed("email", element);
        baseDataValidator.reset().parameter("email").value(email).notBlank();
        
        if(email!=null){
        	final Boolean isValid = email.matches(ClientApiConstants.EMAIL_REGEX);
        	if(!isValid)
        	dataValidationErrors.add(ApiParameterError.parameterError("error.invalid.email.address","Invalid Email Address", "email",email));
        
        } 
        
		final String city = fromApiJsonHelper.extractStringNamed("city", element);
		baseDataValidator.reset().parameter("city").value(city).notBlank().notExceedingLengthOf(100);
		
		final String state = fromApiJsonHelper.extractStringNamed("state", element);
		baseDataValidator.reset().parameter("state").value(state).notBlank().notExceedingLengthOf(100);
		
		final String country = fromApiJsonHelper.extractStringNamed("country", element);
		baseDataValidator.reset().parameter("country").value(country).notBlank().notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}