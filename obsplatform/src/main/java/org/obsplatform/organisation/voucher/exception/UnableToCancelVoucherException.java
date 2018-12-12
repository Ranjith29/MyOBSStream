package org.obsplatform.organisation.voucher.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class UnableToCancelVoucherException extends AbstractPlatformDomainRuleException {

    public UnableToCancelVoucherException() {
        super("error.msg.unable.cancel.voucher.product.type", "unable to cancel voucher");
    }
    
   
   
}
