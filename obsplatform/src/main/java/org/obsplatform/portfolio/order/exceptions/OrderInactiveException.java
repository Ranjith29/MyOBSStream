package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class OrderInactiveException extends AbstractPlatformDomainRuleException {

    

	public OrderInactiveException(Long orderId) {
		 super("error.msg.unable.to.generate.changegroup.request.because.order.is.not.active", "unable generate change group request because order is not active",orderId);
		 
	}
}
