/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.useradministration.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when user resources are not found.
 */
public class UserNotFoundException extends AbstractPlatformResourceNotFoundException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserNotFoundException(final Long id) {
        super("error.msg.user.id.invalid", "User with identifier " + id + " does not exist", id);
    }
}