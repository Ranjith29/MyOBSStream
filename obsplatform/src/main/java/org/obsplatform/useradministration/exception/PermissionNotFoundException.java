/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.useradministration.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when permission resources are not found.
 */
public class PermissionNotFoundException extends AbstractPlatformResourceNotFoundException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PermissionNotFoundException(final String code) {
        super("error.msg.permission.code.invalid", "Permission with Code " + code + " does not exist", code);
    }
}