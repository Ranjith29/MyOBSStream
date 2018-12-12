package org.obsplatform.provisioning.preparerequest.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


@SuppressWarnings("serial")
public class PrepareRequestActivationException extends AbstractPlatformDomainRuleException {

    public PrepareRequestActivationException() {
        super("error.msg.request.sent.for.activation", "Request is already sent for activation");
    }
    
    
    public PrepareRequestActivationException(Long clientId) {
        super("error.msg.request.sent.for.change.credentials", "Request is already sent for change credentials");
    }
}
