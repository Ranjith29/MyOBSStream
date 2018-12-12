package org.obsplatform.finance.payments.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DalpayRequestFailureException extends AbstractPlatformDomainRuleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DalpayRequestFailureException(final Long user){
		 super("error.msg.finance.payment.not.found", "Dalpay Response 'user1' value invalid, user1= " + user);
	}
}