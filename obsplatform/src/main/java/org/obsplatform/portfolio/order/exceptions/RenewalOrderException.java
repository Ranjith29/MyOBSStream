package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class RenewalOrderException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public RenewalOrderException() {
		super("error.msg.renewal.primary.connection.first","secondary connection's renewal depends on primary connection renewal status");

	}

}
