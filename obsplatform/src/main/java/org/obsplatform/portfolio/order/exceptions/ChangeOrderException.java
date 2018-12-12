package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ChangeOrderException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public ChangeOrderException() {
		super("error.msg.change.primary.connection.first","secondary connection's changing depends on primary connection change status");

	}

}
