/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.command;

import java.util.ArrayList;
import java.util.List;

import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a client identifier.
 */
public class ClientIdentifierCommand {

    private final Long documentTypeId;
    private final String documentKey;
    private final String description;

    public ClientIdentifierCommand(final Long documentTypeId, final String documentKey, final String description) {
        this.documentTypeId = documentTypeId;
        this.documentKey = documentKey;
        this.description = description;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public String getDescription() {
        return description;
    }

    public void validateForCreate() {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

    	final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("clientIdentifier");

        baseDataValidator.reset().parameter("documentTypeId").value(this.documentTypeId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("documentKey").value(this.documentKey).notBlank();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

    	final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("clientIdentifier");

        baseDataValidator.reset().parameter("documentKey").value(this.documentKey).ignoreIfNull().notBlank();

        baseDataValidator.reset().anyOfNotNull(this.documentTypeId, this.documentKey);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}