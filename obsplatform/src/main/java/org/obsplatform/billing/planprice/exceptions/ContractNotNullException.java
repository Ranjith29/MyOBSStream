package org.obsplatform.billing.planprice.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ContractNotNullException extends AbstractPlatformDomainRuleException {

	public ContractNotNullException() {
		super("error.msg.contract.duration.cannot.be.null", "Contract duration must not null, please select contract duration value:");
	}

}
