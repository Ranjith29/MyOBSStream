
package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OrderReactivationException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public OrderReactivationException() {
		super("error.msg.reactive.primary.connection.first","secondary connection's reactivation depends on primary connection active status");

	}

}
