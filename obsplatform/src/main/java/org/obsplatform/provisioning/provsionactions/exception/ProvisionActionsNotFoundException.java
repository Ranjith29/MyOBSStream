package org.obsplatform.provisioning.provsionactions.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class ProvisionActionsNotFoundException extends AbstractPlatformDomainRuleException {

    
	public ProvisionActionsNotFoundException() {
        super("error.msg.billing.provision.action.not.found", "provisioninga action is does not exist");
    }

	public ProvisionActionsNotFoundException(String provisionId) {
		
		 super("error.msg.billing.provision.action.not.found", "provisioninga action is does not exist",provisionId);
	}
}
