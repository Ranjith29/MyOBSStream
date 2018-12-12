package org.obsplatform.billing.selfcare.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfCareNotVerifiedException extends AbstractPlatformDomainRuleException {

	public SelfCareNotVerifiedException(final String emailId){
		 super("error.msg.billing.selfcare.not.verified", "Email Verification Not Verified with this " + emailId);
	}
	
}