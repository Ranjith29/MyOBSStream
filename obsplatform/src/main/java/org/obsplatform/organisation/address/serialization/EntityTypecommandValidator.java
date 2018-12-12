package org.obsplatform.organisation.address.serialization;

import java.util.ArrayList;
import java.util.List;

import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.organisation.address.data.EntityTypecommand;


public class EntityTypecommandValidator {

	private final EntityTypecommand command;

	public EntityTypecommandValidator(final EntityTypecommand command) {
		this.command=command;
	}


	public void validateForCreate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("address");
		baseDataValidator.reset().parameter("entityCode").value(command.getEntityCode()).integerGreaterThanZero().notNull().notBlank();
		baseDataValidator.reset().parameter("entityName").value(command.getEntityName()).notBlank().notNull();

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}
