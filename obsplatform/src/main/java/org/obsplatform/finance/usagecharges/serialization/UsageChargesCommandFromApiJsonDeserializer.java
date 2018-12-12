package org.obsplatform.finance.usagecharges.serialization;

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
public class UsageChargesCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId","number","time","destinationNumber","destinationLocation","duration","cost","locale", "dateFormat","internalId"));

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public UsageChargesCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charges");

		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final Long clientId =this.fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();
		
		final String number =this.fromApiJsonHelper.extractStringNamed("number", element);
		baseDataValidator.reset().parameter("number").value(number).notBlank().notExceedingLengthOf(16);
		
		final String time =this.fromApiJsonHelper.extractStringNamed("time", element);
		baseDataValidator.reset().parameter("time").value(time).notBlank();
		
		final BigDecimal  duration = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("duration", element);
		baseDataValidator.reset().parameter("duration").value(duration).notBlank().notLessThanMin(BigDecimal.ZERO);
		
		final BigDecimal cost =this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("cost", element);
		baseDataValidator.reset().parameter("cost").value(cost).notBlank().notLessThanMin(BigDecimal.ZERO);
		
		final Long internalId =this.fromApiJsonHelper.extractLongNamed("internalId", element);
		baseDataValidator.reset().parameter("internalId").value(internalId).notBlank();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

}
