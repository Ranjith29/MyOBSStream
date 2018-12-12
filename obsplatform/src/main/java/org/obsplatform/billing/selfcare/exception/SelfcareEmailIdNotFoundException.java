package org.obsplatform.billing.selfcare.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfcareEmailIdNotFoundException extends AbstractPlatformDomainRuleException{

	public SelfcareEmailIdNotFoundException(final String uniqueReference){
		super("error.msg.selfcare.emailid.not.found", "Email Address not found with this " + uniqueReference, uniqueReference);
	}
	
	
}
