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
public class TicketMasterFromApiJsonDeserializer {
	
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("priority", "problemCode", "description", "assignedTo", "ticketDate", "ticketTime", "locale", "dateFormat",
														"sourceOfTicket", "dueTime", "ticketURL","status","resolutionDescription", "fileLocation","issue","assignFrom","notes","departmentId",
														"appointmentDate","appointmentTime","nextCallDate","nextCallTime","resolutionDate"));
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public TicketMasterFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	/**
	  * Validation for Create Ticket
	  * */
	 public void validateForCreate(final String json) {
		 
		 if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
	        
	     final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
	     fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
	        
	     final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	     final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("ticketmaster");
	        
	     final JsonElement element = fromApiJsonHelper.parse(json);
	        
	     final String priority = fromApiJsonHelper.extractStringNamed("priority", element);
	     final String problemCode = fromApiJsonHelper.extractStringNamed("problemCode", element);
	     final String description = fromApiJsonHelper.extractStringNamed("description", element);
	     final String assignedTo = fromApiJsonHelper.extractStringNamed("assignedTo", element);
	     final String status = fromApiJsonHelper.extractStringNamed("status", element);
	     final Long departmentId = fromApiJsonHelper.extractLongNamed("departmentId", element);

	     baseDataValidator.reset().parameter("problemCode").value(problemCode).notBlank().notExceedingLengthOf(100);
	     baseDataValidator.reset().parameter("priority").value(priority).notBlank().notExceedingLengthOf(100);
	     baseDataValidator.reset().parameter("assignedTo").value(assignedTo).notBlank().notExceedingLengthOf(100);
	     baseDataValidator.reset().parameter("description").value(description).notBlank();
	     baseDataValidator.reset().parameter("issue").value(description).notBlank().notExceedingLengthOf(200);
	     baseDataValidator.reset().parameter("status").value(status).notBlank().notExceedingLengthOf(100);
	     baseDataValidator.reset().parameter("status").value(status).notBlank();
	     throwExceptionIfValidationWarningsExist(dataValidationErrors);
	     
	 }
	 
	 /**
	  * Validation for Close Ticket
	  * */
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

	 /**
	  * Validation for Update Ticket
	  * */
	 public void validateForUpdate(final String json) {
	        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
	        
	        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
	        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
	        
	        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("ticketmaster");
	        
	        final JsonElement element = fromApiJsonHelper.parse(json);
	        if(fromApiJsonHelper.parameterExists("status", element)){
	        	final String status = fromApiJsonHelper.extractStringNamed("status", element);
	        	baseDataValidator.reset().parameter("status").value(status).notBlank().notExceedingLengthOf(100);
	        }
	        if(fromApiJsonHelper.parameterExists("notes", element)){
	            final String notes = fromApiJsonHelper.extractStringNamed("notes", element);
	            baseDataValidator.reset().parameter("notes").value(notes).notBlank();
	        }
	        if(fromApiJsonHelper.parameterExists("resolutionDescription", element)){
	        	final String resolutionDescription = fromApiJsonHelper.extractStringNamed("resolutionDescription", element);
	        	baseDataValidator.reset().parameter("resolutionDescription").value(resolutionDescription).notBlank();
	        }
	        if(fromApiJsonHelper.parameterExists("assignedTo", element)){
	        	final Long assignedTo = fromApiJsonHelper.extractLongNamed("assignedTo", element);
	            baseDataValidator.reset().parameter("assignedTo").value(assignedTo).notBlank().notExceedingLengthOf(100);
	        }
	        if(fromApiJsonHelper.parameterExists("nextCallDate", element)){
	        	final String nextCallDate = fromApiJsonHelper.extractStringNamed("nextCallDate", element);
	        	baseDataValidator.reset().parameter("nextCallDate").value(nextCallDate).notBlank();
	        }
	        if(fromApiJsonHelper.parameterExists("nextCallTime", element)){
	        	final String nextCallTime = fromApiJsonHelper.extractStringNamed("nextCallTime", element);
	        	baseDataValidator.reset().parameter("nextCallTime").value(nextCallTime).notBlank();
	        }
	        if(fromApiJsonHelper.parameterExists("appointmentDate", element)){
	        	final String appointmentDate = fromApiJsonHelper.extractStringNamed("appointmentDate", element);
	        	baseDataValidator.reset().parameter("appointmentDate").value(appointmentDate).notBlank();
	        }
	        if(fromApiJsonHelper.parameterExists("appointmentTime", element)){
	        	final String appointmentTime = fromApiJsonHelper.extractStringNamed("appointmentTime", element);
	        	baseDataValidator.reset().parameter("appointmentTime").value(appointmentTime).notBlank();
	        }
	        
	        throwExceptionIfValidationWarningsExist(dataValidationErrors);
		}
	 
	 private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
	        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
	                "Validation errors exist.", dataValidationErrors);
	        }
	 }	
}