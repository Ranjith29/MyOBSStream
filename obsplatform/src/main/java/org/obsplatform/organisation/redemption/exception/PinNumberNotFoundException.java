package org.obsplatform.organisation.redemption.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PinNumberNotFoundException extends AbstractPlatformDomainRuleException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PinNumberNotFoundException(final String pinNumber) {
		super("error.msg.redemption.pinNumber.invalid", "PinNumber with this " + pinNumber + " does not exist", pinNumber);
    }

}
