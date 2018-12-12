package org.obsplatform.portfolio.service.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
public final class PriceCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id","planCode","locale","serviceCode","chargeCode","chargeVariant","price",
    		"discountId","priceregion","duration","isPrepaid","amount"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public PriceCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }
    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("price");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String chargeCode = fromApiJsonHelper.extractStringNamed("chargeCode", element);
        baseDataValidator.reset().parameter("chargeCode").value(chargeCode).notBlank().notExceedingLengthOf(10);
        final Long discountId = fromApiJsonHelper.extractLongNamed("discountId", element);
        baseDataValidator.reset().parameter("discountId").value(discountId).notBlank();
        final Long chargeVariant = fromApiJsonHelper.extractLongNamed("chargeVariant", element);
        baseDataValidator.reset().parameter("chargeVariant").value(chargeVariant).notNull();
       final Long priceregion = fromApiJsonHelper.extractLongNamed("priceregion", element);
        baseDataValidator.reset().parameter("priceregion").value(priceregion).notNull();
        final BigDecimal price = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
        baseDataValidator.reset().parameter("price").value(price).notNull();
        final boolean isPrepaid=fromApiJsonHelper.extractBooleanNamed("isPrepaid", element);
        	if(isPrepaid){
        		final String duration=fromApiJsonHelper.extractStringNamed("duration", element);
            	baseDataValidator.reset().parameter("duration").value(duration).notBlank();
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
        
        final boolean isPrepaid=fromApiJsonHelper.extractBooleanNamed("isPrepaid", element);
    	if(isPrepaid){
    		final String duration=fromApiJsonHelper.extractStringNamed("duration", element);
        	baseDataValidator.reset().parameter("duration").value(duration).notBlank();
    	}

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}