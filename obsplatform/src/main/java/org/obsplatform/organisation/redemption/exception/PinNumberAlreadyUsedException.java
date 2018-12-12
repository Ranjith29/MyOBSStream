package org.obsplatform.organisation.redemption.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PinNumberAlreadyUsedException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PinNumberAlreadyUsedException(final String pinNumber) {
		super("error.msg.redemption.pinNumber.already.use", "PinNumber with this " + pinNumber + " already Used", pinNumber);
    }
}
