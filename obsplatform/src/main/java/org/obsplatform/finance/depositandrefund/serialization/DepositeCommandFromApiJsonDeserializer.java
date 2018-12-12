package org.obsplatform.finance.depositandrefund.serialization;

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

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

/**
 * @author hugo Deserializer for code JSON to validate API request.
 */
@Component
public class DepositeCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	final private Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId", "feeId","refundMode","refundAmount","locale","transactionType", "categoryId"));
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public DepositeCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	/**
	 * @param json
	 * check validation for create charge codes
	 */

	public void validaForCreate(String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = 1L;}.getType();

		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposite");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String clientId = fromApiJsonHelper.extractStringNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();

		final String feeId = fromApiJsonHelper.extractStringNamed("feeId", element);
		baseDataValidator.reset().parameter("feeId").value(feeId).notBlank();
		
		/*final String refundAmount = fromApiJsonHelper.extractStringNamed("refundAmount", element);
		baseDataValidator.reset().parameter("refundAmount").value(refundAmount).notBlank();*/

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	/**
	 * @param json
	 */
	public void validaForCreateRefund(final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final  Type typeOfMap = new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = 1L;}.getType();

		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("refund");
		
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final Long refundMode = fromApiJsonHelper.extractLongNamed("refundMode", element);
		baseDataValidator.reset().parameter("refundMode").value(refundMode).notBlank();
		
		final BigDecimal refundAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("refundAmount", element);
		baseDataValidator.reset().parameter("refundAmount").value(refundAmount).notBlank();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
	
	/**
	 * @param json
	 * check validation for create Registration charge codes
	 */

	public void validateForCreateRegistrationFee(String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = 1L;}.getType();

		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("registraationfee");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String clientId = fromApiJsonHelper.extractStringNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();

		final String transactionType = fromApiJsonHelper.extractStringNamed("transactionType", element);
		baseDataValidator.reset().parameter("transactionType").value(transactionType).notBlank();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}
}
