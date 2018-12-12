/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.exception;

/**
 * {@link RuntimeException} thrown when an invalid tenant identifier is used in
 * request to platform.
 * 
 * @see CustomRequestHeaderAuthenticationFilter
 */
public class InvalidTenantIdentiferException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidTenantIdentiferException(final String message) {
        super(message);
    }
}