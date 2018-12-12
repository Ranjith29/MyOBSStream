package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class NoDurationFound extends AbstractPlatformDomainRuleException {

    public NoDurationFound(String msg) {
        super("error.msg.duration.not.found.in.plan.price", " Duration with "+msg+" not available in Plan price",msg);
    }
    
   
}
