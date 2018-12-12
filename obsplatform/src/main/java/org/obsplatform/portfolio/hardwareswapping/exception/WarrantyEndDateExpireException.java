package org.obsplatform.portfolio.hardwareswapping.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class WarrantyEndDateExpireException  extends AbstractPlatformDomainRuleException {

	public WarrantyEndDateExpireException(String serialNo) {
		
		super("error.msg.hardware.swapping.warranty.expiry", "Couldn't swap Hardware because hardware warranty date expired", serialNo);
	}

}
