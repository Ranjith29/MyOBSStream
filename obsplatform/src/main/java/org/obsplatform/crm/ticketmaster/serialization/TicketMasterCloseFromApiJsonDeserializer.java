package org.obsplatform.crm.ticketmaster.serialization;

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
public class TicketMasterCloseFromApiJsonDeserializer {

private final FromJsonHelper fromApiJsonHelper;
	
private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("status","resolutionDescription","ticketURL"));	

	@Autowired
	public TicketMasterCloseFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	
	public void validateForClose(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("ticketmaster");
        
        final JsonElement element = fromApiJsonHelper.parse(json);
        
        Integer status = null;
        String resolutionDescription = null;
        
        if(fromApiJsonHelper.parameterExists("status", element)){
        	status= Integer.parseInt( fromApiJsonHelper.extractStringNamed("status", element));
        }
        if(fromApiJsonHelper.parameterExists("resolutionDescription", element)){
        	resolutionDescription = fromApiJsonHelper.extractStringNamed("resolutionDescription", element);
        }
        
        
        

        baseDataValidator.reset().parameter("status").value(status).notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("resolutionDescription").value(resolutionDescription).notBlank().notExceedingLengthOf(100);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
 
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
