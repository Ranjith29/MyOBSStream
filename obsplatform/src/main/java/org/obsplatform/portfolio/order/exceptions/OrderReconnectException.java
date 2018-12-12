
package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OrderReconnectException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public OrderReconnectException() {
		super("error.msg.reconnect.primary.connection.first","secondary connection's reconnection depends on primary connection reconnect status");

	}

}
