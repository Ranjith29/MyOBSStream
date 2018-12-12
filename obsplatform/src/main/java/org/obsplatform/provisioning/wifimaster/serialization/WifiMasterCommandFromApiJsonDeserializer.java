package org.obsplatform.provisioning.wifimaster.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.InvalidJsonException;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;


/**
 * Deserializer for code JSON to validate API request.
 */

@Component
public final class WifiMasterCommandFromApiJsonDeserializer {

	  /**
     * The parameters supported for this command.
     */
	
	private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("ssid","clientId","wifiPassword","serviceType","id","orderId","serviceId"));
	
	private final FromJsonHelper fromJsonHelper;
	
	@Autowired
	public WifiMasterCommandFromApiJsonDeserializer(final FromJsonHelper fromJsonHelper) {
		this.fromJsonHelper = fromJsonHelper;
	}
	
	public void validateForCreate(final JsonCommand command){
		
		if(StringUtils.isBlank(command.toString())){
			throw new InvalidJsonException();
		}
		
		final Type typeOfMap = new TypeToken<Map<String,Object>>(){}.getType();
		fromJsonHelper.checkForUnsupportedParameters(typeOfMap, command.json(), supportedParams);
		
		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("wifi");
		
		
		final String ssid = command.stringValueOfParameterNamed("ssid");
		baseDataValidator.reset().parameter("ssid").value(ssid).notExceedingLengthOf(20);
		
		final String wifiPassword = command.stringValueOfParameterNamed("wifiPassword");
		baseDataValidator.reset().parameter("wifiPassword").value(wifiPassword).notExceedingLengthOf(20);
		
		/*final Long clientId=command.longValueOfParameterNamed("clientId");
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank().notExceedingLengthOf(20);
		
		final String serviceType = command.stringValueOfParameterNamed("serviceType");
		baseDataValidator.reset().parameter("serviceType").value(serviceType).notExceedingLengthOf(20);
		
		final Long orderId=command.longValueOfParameterNamed("orderId");
		baseDataValidator.reset().parameter("orderId").value(orderId).notBlank().notExceedingLengthOf(20);
		
		final Long serviceId=command.longValueOfParameterNamed("serviceId");
		baseDataValidator.reset().parameter("serviceId").value(serviceId).notExceedingLengthOf(20);*/
		
		
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
	
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
