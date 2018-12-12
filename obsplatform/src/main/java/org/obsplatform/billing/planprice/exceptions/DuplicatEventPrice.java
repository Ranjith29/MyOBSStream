package org.obsplatform.billing.planprice.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class DuplicatEventPrice extends AbstractPlatformDomainRuleException {

	public DuplicatEventPrice(final String formatType) {
		super("event.is.already.exists.with.format", "plan is already existed with format:"+formatType, formatType);
	}

}
