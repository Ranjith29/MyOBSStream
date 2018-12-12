package org.obsplatform.billing.selfcare.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfCareAlreadyVerifiedException extends AbstractPlatformDomainRuleException {

	public SelfCareAlreadyVerifiedException(final String verifiedKey){
		 super("error.msg.billing.generatedKey.already.verified", "generatedKey already verified with this " + verifiedKey);
	}
	
}
