package org.obsplatform.billing.chargecode.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * @author hugo
 *  this class {@link RuntimeException} thrown when a code is not found.
 */

public class ChargeCodeNotFoundException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param chargeCodeId
	 */
	public ChargeCodeNotFoundException(final String chargeCodeId) {
		super("error.msg.chargeCode.not.found", "chargeCode with this id" + chargeCodeId + "not exist", chargeCodeId);

	}

}