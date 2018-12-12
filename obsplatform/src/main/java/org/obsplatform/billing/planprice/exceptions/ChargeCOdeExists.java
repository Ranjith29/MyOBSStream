package org.obsplatform.billing.planprice.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ChargeCOdeExists extends AbstractPlatformDomainRuleException {

	public ChargeCOdeExists(final String chrgeCode) {
		super("plan.is.already.exists.with.charge.code", "plan is already existed with charge code:"+chrgeCode, chrgeCode);
	}

}
