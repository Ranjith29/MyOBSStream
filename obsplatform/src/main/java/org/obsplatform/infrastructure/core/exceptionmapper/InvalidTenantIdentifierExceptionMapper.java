/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obsplatform.infrastructure.core.data.ApiGlobalErrorResponse;
import org.obsplatform.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link InvalidTenantIdentiferException}
 * thrown by platform during authentication into a HTTP API friendly format.
 * 
 * The {@link InvalidTenantIdentiferException} is thrown by spring security on
 * platform when a request contains an invalid tenant identifier.
 */
@Provider
@Component
@Scope("singleton")
public class InvalidTenantIdentifierExceptionMapper implements ExceptionMapper<InvalidTenantIdentiferException> {

    @Override
    public Response toResponse(@SuppressWarnings("unused") final InvalidTenantIdentiferException e) {
        return Response.status(Status.UNAUTHORIZED).entity(ApiGlobalErrorResponse.invalidTenantIdentifier())
                .type(MediaType.APPLICATION_JSON).build();
    }
}