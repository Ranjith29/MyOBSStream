/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.logistics.ownedhardware.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when a code is not found.
 */
public class OwnHardwareNotFoundException extends AbstractPlatformResourceNotFoundException {


	private static final long serialVersionUID = 1L;

	public OwnHardwareNotFoundException(final String id) {
        super("error.msg.ownhardware.not.found", "Ownhardware with this id"+id+"not exist",id);
        
    }

   
}