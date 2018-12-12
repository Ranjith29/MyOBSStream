package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class NoGrnIdFoundException extends AbstractPlatformDomainRuleException {

    

	public NoGrnIdFoundException(Long orderId) {
		 super("error.msg.order.quantity..exceeds", "Grn id "+orderId+" not found. ",orderId);
		 
	}
}
