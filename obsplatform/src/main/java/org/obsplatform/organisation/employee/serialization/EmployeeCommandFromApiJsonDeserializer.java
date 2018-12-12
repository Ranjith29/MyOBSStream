package org.obsplatform.organisation.employee.serialization;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class EmployeeCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "name", "loginname", "password","repeatpassword","phone", "email", "departmentId","isprimary","roleName"));

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public EmployeeCommandFromApiJsonDeserializer(
			final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() { }.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap,json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("employee");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String name = fromApiJsonHelper.extractStringNamed("name",element);
		final String loginname = fromApiJsonHelper.extractStringNamed("loginname", element);
		final String password = fromApiJsonHelper.extractStringNamed("password", element);
		final String repeatpassword = fromApiJsonHelper.extractStringNamed("repeatpassword", element);
		final String phone = fromApiJsonHelper.extractStringNamed("phone",element);
		final String email = fromApiJsonHelper.extractStringNamed("email",element);
		final Long departmentId = fromApiJsonHelper.extractLongNamed("departmentId", element);
		final boolean isprimary = fromApiJsonHelper.extractBooleanNamed("isprimary", element);
		

		
		baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(30);
		baseDataValidator.reset().parameter("loginname").value(loginname).notBlank().notExceedingLengthOf(30);
		baseDataValidator.reset().parameter("password").value(password).notBlank();
		baseDataValidator.reset().parameter("phone").value(phone).notNull().notBlank().validateforNumeric().notExceedingLengthOf(15);
		baseDataValidator.reset().parameter("email").value(email).notBlank().validateEmailExpresstion(email);
		baseDataValidator.reset().parameter("departmentId").value(departmentId).notBlank();
		baseDataValidator.reset().parameter("isprimary").value(isprimary);
		
		if (StringUtils.isNotBlank(password)) {
            baseDataValidator.reset().parameter("password").value(password).equalToParameter("repeatPassword", repeatpassword);
        }
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() { }.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap,json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("employee");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String name = fromApiJsonHelper.extractStringNamed("name",element);
		final String loginname = fromApiJsonHelper.extractStringNamed("loginname", element);
		final String password = fromApiJsonHelper.extractStringNamed("password", element);
		final String repeatpassword = fromApiJsonHelper.extractStringNamed("repeatpassword", element);
		final String phone = fromApiJsonHelper.extractStringNamed("phone",element);
		final String email = fromApiJsonHelper.extractStringNamed("email",element);
		final String departmentId = fromApiJsonHelper.extractStringNamed("departmentId", element);
		final boolean isprimary = fromApiJsonHelper.extractBooleanNamed("isprimary", element);
		

		
		baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(30);
		baseDataValidator.reset().parameter("loginname").value(loginname).notBlank().notExceedingLengthOf(30);
		baseDataValidator.reset().parameter("password").value(password).notBlank();
		baseDataValidator.reset().parameter("phone").value(phone).notNull().notBlank().validateforNumeric().notExceedingLengthOf(15);
		baseDataValidator.reset().parameter("email").value(email).notBlank().validateEmailExpresstion(email);
		baseDataValidator.reset().parameter("departmentId").value(departmentId).notBlank();
		baseDataValidator.reset().parameter("isprimary").value(isprimary);
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

}
