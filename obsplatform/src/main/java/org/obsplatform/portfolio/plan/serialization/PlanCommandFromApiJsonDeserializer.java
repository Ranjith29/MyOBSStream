package org.obsplatform.portfolio.plan.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class PlanCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("planCode","planDescription","locale",
    		"dateFormat","startDate","endDate","status","chargeCode","roles","billRule","isHwReq",
    		"provisioingSystem","services","duration","volume","isPrepaid","units","allowTopup", "events","planNotes","trialDays", "clientCategorys"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public PlanCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("plan");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String planCode = fromApiJsonHelper.extractStringNamed("planCode", element);
        baseDataValidator.reset().parameter("planCode").value(planCode).notBlank().notExceedingLengthOf(10);
        final String planDescription = fromApiJsonHelper.extractStringNamed("planDescription", element);
        baseDataValidator.reset().parameter("planDescription").value(planDescription).notBlank();
        final LocalDate startDate = fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        baseDataValidator.reset().parameter("startDate").value(startDate).notBlank();
        final Long status = fromApiJsonHelper.extractLongNamed("status", element);
        baseDataValidator.reset().parameter("status").value(status).notNull();
        final Long billRule = fromApiJsonHelper.extractLongNamed("billRule", element);
        baseDataValidator.reset().parameter("billRule").value(billRule).notNull();
        final String provisioingSystem = fromApiJsonHelper.extractStringNamed("provisioingSystem", element);
        baseDataValidator.reset().parameter("provisioingSystem").value(provisioingSystem).notBlank();
        final String[] services = fromApiJsonHelper.extractArrayNamed("services", element);
        baseDataValidator.reset().parameter("services").value(services).arrayNotEmpty();
        final String[] clientCategorys = fromApiJsonHelper.extractArrayNamed("clientCategorys", element);
        baseDataValidator.reset().parameter("clientCategorys").value(clientCategorys).arrayNotEmpty();
        
        if(fromApiJsonHelper.parameterExists("events", element)){
        	 final String[] events = fromApiJsonHelper.extractArrayNamed("events", element);
             baseDataValidator.reset().parameter("events").value(events).arrayNotEmpty();
        }
       
        final boolean isPrepaid=fromApiJsonHelper.extractBooleanNamed("isPrepaid", element);
        if(isPrepaid){
        	final String volumeType=fromApiJsonHelper.extractStringNamed("volume", element);
        	baseDataValidator.reset().parameter("volume").value(volumeType).notNull();
        	final BigDecimal units=fromApiJsonHelper.extractBigDecimalWithLocaleNamed("units", element);
        	baseDataValidator.reset().parameter("units").value(units).notNull();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("code");

        final JsonElement element = fromApiJsonHelper.parse(json);
        if (fromApiJsonHelper.parameterExists("name", element)) {
            final String name = fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}