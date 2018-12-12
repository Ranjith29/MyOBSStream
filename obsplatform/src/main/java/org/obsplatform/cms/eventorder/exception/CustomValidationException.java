package org.obsplatform.cms.eventorder.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class CustomValidationException extends AbstractPlatformDomainRuleException {

    public CustomValidationException() {
        super("error.msg.client.plan.details.not.found", "Client has no Prepaid Plans ");
    }
    
   
}
