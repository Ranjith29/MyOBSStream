/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.logistics.onetimesale.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when the Item code and serial number not matching.
 */
public class ItemCodeSerialNumberNotMatchedException extends AbstractPlatformDomainRuleException {

    /* * */
	private static final long serialVersionUID = 1L;

	public ItemCodeSerialNumberNotMatchedException(final String msg) {
        super("error.msg.itemcode.and.serial.number.not.matched",
        		"Item Code Not Matched for this Item Check Serial Number and Item Code", msg);
        
    }
	

}