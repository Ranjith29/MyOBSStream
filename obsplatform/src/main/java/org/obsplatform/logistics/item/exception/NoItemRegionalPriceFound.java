package org.obsplatform.logistics.item.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class NoItemRegionalPriceFound extends AbstractPlatformDomainRuleException {

    public NoItemRegionalPriceFound() {
        super("error.msg.item.regional.price.not.found", " No Regional Price Available for this Item");
    }
    
   
}
