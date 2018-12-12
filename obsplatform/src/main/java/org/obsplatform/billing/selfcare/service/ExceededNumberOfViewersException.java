package org.obsplatform.billing.selfcare.service;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.obsplatform.portfolio.client.exception.ClientStatusException;

public class ExceededNumberOfViewersException extends AbstractPlatformDomainRuleException {
	public ExceededNumberOfViewersException(final Long clientId) {
		super("error.msg.exceeded.no.of.Viewers", "Maximum Number of allowable viewers exceeded with this Client");
		// TODO Auto-generated constructor stub
	}

}
