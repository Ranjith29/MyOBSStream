package org.obsplatform.billing.selfcare.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfCareTemporaryEmailIdNotFoundException extends AbstractPlatformDomainRuleException{

	public SelfCareTemporaryEmailIdNotFoundException(final String emailId){
		 super("error.msg.billing.userName.not.found", "userName/EmailId not found with this " + emailId);
	}
}
