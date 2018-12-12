

package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OrderDisconnectException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public OrderDisconnectException() {
		super("error.msg.disconnect.primary.connection.first","secondary connection's disconnection depends on primary connection disconnect status");

	}

}


