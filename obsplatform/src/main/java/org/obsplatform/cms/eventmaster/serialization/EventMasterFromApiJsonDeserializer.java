package org.obsplatform.cms.eventmaster.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.InvalidJsonException;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * {@link Component} Class for Validating Json for {@link EventMaster}
 * 
 * @author pavani
 * @author Rakesh
 */
@Component
@SuppressWarnings("serial")
public class EventMasterFromApiJsonDeserializer {

	private final Set<String> supportedParameters = new HashSet<String> (Arrays.asList("eventName","chargeCode","eventStartDate","eventDescription","status","eventEndDate","allowCancellation","eventValidity","mediaData","locale","dateFormat","eventCategory","removemedia"));
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public EventMasterFromApiJsonDeserializer (final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	public void validateForCreate(final String json) {
		if(StringUtils.isBlank(json)) { 
			throw new InvalidJsonException();
		}
		
		final Type typeOfMap = new TypeToken<Map<String,Object>>() {}.getType(); 
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
		
		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	    final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("eventmaster");
	     
	    final JsonElement element = fromApiJsonHelper.parse(json);
	     
	    final String eventName = fromApiJsonHelper.extractStringNamed("eventName", element);
	    baseDataValidator.reset().parameter("eventName").value(eventName).notBlank().notExceedingLengthOf(10);
	    
	    final String chargeCode = fromApiJsonHelper.extractStringNamed("chargeCode", element);
	    baseDataValidator.reset().parameter("chargeCode").value(chargeCode).notBlank().notExceedingLengthOf(10);
	    
	    final String eventStartDate = fromApiJsonHelper.extractStringNamed("eventStartDate", element);
        baseDataValidator.reset().parameter("eventStartDate").value(eventStartDate).notBlank();
        
        final String eventValidity = fromApiJsonHelper.extractStringNamed("eventValidity", element);
        baseDataValidator.reset().parameter("eventValidity").value(eventValidity).notBlank();
	    
	    if(fromApiJsonHelper.parameterExists("removemedia", element)){
	    	
	    	final JsonArray mediaData = fromApiJsonHelper.extractJsonArrayNamed("mediaData", element);
			int DataSize = mediaData.size();
			baseDataValidator.reset().parameter("mediaData").value(DataSize).integerGreaterThanZero(); 
	    }else{
	    	final String[] services = fromApiJsonHelper.extractArrayNamed("mediaData", element);
		    baseDataValidator.reset().parameter("services").value(services).arrayNotEmpty();
	    }
	    
	    final Long status = fromApiJsonHelper.extractLongNamed("status", element);
	    baseDataValidator.reset().parameter("status").value(status).notNull();
	    
	    final String eventCategory = fromApiJsonHelper.extractStringNamed("eventCategory", element);
		baseDataValidator.reset().parameter("eventCategory").value(eventCategory).notBlank().notExceedingLengthOf(50);  
		
		if("Live Event".equalsIgnoreCase(eventCategory)){
			final String eventEndDate = fromApiJsonHelper.extractStringNamed("eventEndDate", element);
	        baseDataValidator.reset().parameter("eventEndDate").value(eventEndDate).notNull();
	        if(eventStartDate != null && !eventStartDate.contains(":")){
	        	dataValidationErrors.add(ApiParameterError.parameterError("Event StartTime is mandatory for live event", "event.start.time.is.mandatory.for.live.event", "starttime","event.start.time.is.mandatory.for.live.event"));
	        }if(eventEndDate != null && !eventEndDate.contains(":")){
	        	dataValidationErrors.add(ApiParameterError.parameterError("Event EndTime is mandatory for live event", "event.end.time.is.mandatory.for.live.event", "EndTime", "event.end.time.is.mandatory.for.live.event"));
	        }
		}
	     
	     throwExceptionIfValidationWarningsExist(dataValidationErrors);
	     
	}
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
