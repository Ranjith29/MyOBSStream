package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OrderNotFoundException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public OrderNotFoundException(final Long orderId) {
		super("error.msg.Order.not.found.with.this.identifier","Order not found with this identifier", orderId);

	}

	public OrderNotFoundException() {
		super("error.msg.disconnect.secondary.connection.before.disconnect.primary.connection","this primary connection contain secondary connection's");

	}

	public OrderNotFoundException(final String orderAction) {
		super("error.msg.change.primary.connection.before.change.secondary.connection","this secondary connection actions always depends on primary connection");
	
	}

}
