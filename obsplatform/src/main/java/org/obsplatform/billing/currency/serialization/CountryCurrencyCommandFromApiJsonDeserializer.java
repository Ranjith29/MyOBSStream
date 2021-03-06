package org.obsplatform.billing.currency.serialization;

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
public final class CountryCurrencyCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("country", "currency", "status", "baseCurrency",
					"conversionRate", "locale", "countryISD"));
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public CountryCurrencyCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	/**
	 * @param json
	 * check validation for create country currency configuration
	 */
	public void validateForCreate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("countrycurrency");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String country = fromApiJsonHelper.extractStringNamed("country",element);
		baseDataValidator.reset().parameter("country").value(country).notBlank();
		
		final String currency = fromApiJsonHelper.extractStringNamed("currency", element);
		baseDataValidator.reset().parameter("currency").value(currency).notBlank();
		
		final String status = fromApiJsonHelper.extractStringNamed("status",element);
		baseDataValidator.reset().parameter("status").value(status).notBlank();
		
		final String baseCurrency = fromApiJsonHelper.extractStringNamed("baseCurrency",element);
		baseDataValidator.reset().parameter("baseCurrency").value(baseCurrency).notBlank();
		
		final BigDecimal conversionRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("conversionRate", element);
		baseDataValidator.reset().parameter("conversionRate").value(conversionRate).notNull();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}