package org.obsplatform.organisation.department.serialization;

import java.lang.reflect.Type;
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

@Component
public class DepartmentCommandFromApiJsonDeserializer {
	
	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(
			Arrays.asList("id", "deptname", "deptdescription", "officeid"));

	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public DepartmentCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper){
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(String json) {


			if (StringUtils.isBlank(json)) {
				throw new InvalidJsonException();
			}

			final Type typeOfMap = new TypeToken<Map<String, Object>>() { }.getType();
			fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

			final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("department");

			final JsonElement element = fromApiJsonHelper.parse(json);
			
			final String deptname = fromApiJsonHelper.extractStringNamed("deptname", element);
			final Long officeid = fromApiJsonHelper.extractLongNamed("officeid", element);
			
			baseDataValidator.reset().parameter("deptname").value(deptname).notBlank().notExceedingLengthOf(100);
			baseDataValidator.reset().parameter("officeid").value(officeid).notBlank();

			throwExceptionIfValidationWarningsExist(dataValidationErrors);
		}

		private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
			if (!dataValidationErrors.isEmpty()) {
				throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
						"Validation errors exist.", dataValidationErrors);
			}
		}
		
		public void validateForUpdate(final String json) {
	        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

	        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
	        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

	        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("department");

	        final JsonElement element = this.fromApiJsonHelper.parse(json);

	        final String deptname = fromApiJsonHelper.extractStringNamed("deptname", element);
			final Long officeid = fromApiJsonHelper.extractLongNamed("officeid", element);
			
			baseDataValidator.reset().parameter("deptname").value(deptname).notBlank().notExceedingLengthOf(100);
			baseDataValidator.reset().parameter("officeid").value(officeid).notBlank();
			
	        throwExceptionIfValidationWarningsExist(dataValidationErrors);
	    }
	}
