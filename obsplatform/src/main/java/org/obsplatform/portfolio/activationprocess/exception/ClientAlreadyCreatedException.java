package org.obsplatform.portfolio.activationprocess.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientAlreadyCreatedException extends AbstractPlatformDomainRuleException {

	public ClientAlreadyCreatedException() {
		super("error.msg.billing.client.already.created", "client already Created.");
	}

}

