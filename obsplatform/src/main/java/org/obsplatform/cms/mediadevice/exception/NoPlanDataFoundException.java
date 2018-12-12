package org.obsplatform.cms.mediadevice.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class NoPlanDataFoundException extends AbstractPlatformDomainRuleException {

    public NoPlanDataFoundException() {
        super("error.msg.device.details.not.found", "Device Details are does not exist ");
    }
    
   
}
