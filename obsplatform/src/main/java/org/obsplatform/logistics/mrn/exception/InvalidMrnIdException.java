package org.obsplatform.logistics.mrn.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidMrnIdException extends AbstractPlatformDomainRuleException {

 
    
    public InvalidMrnIdException(String mrnId) {
        super("error.msg.invalid.mrn.id", "Invalid Mrn Id", mrnId);
    }

}
