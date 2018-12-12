package org.obsplatform.finance.payments.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class KortaRequestFailureException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KortaRequestFailureException(final Long user){
		 super("error.msg.finance.payment.korta.client.not.found", "Invalid clientId Parameter");
	}
	
}
