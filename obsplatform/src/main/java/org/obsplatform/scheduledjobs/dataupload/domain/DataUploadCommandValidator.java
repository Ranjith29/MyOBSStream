package org.obsplatform.scheduledjobs.dataupload.domain;

import java.util.ArrayList;
import java.util.List;

import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.scheduledjobs.dataupload.command.DataUploadCommand;

public class DataUploadCommandValidator {

	
	private final DataUploadCommand command;
	
	
	public DataUploadCommandValidator(final DataUploadCommand command) {
		this.command=command;
	}

	public void validateForCreate(){
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("UploadStatus");
			baseDataValidator.reset().parameter("fileName").value(command.getFileName()).notNull();
			baseDataValidator.reset().parameter("description").value(command.getDescription()).notNull();
			
			if (!dataValidationErrors.isEmpty()) {
				throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
			}
		}
	}


