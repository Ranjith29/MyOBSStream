package org.obsplatform.finance.paymentsgateway.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


@SuppressWarnings("serial")
public class PaypalStatusChangeActionTypeMisMatchException extends AbstractPlatformDomainRuleException{

	public PaypalStatusChangeActionTypeMisMatchException(String message) {
		
		super("error.msg.paypal.StatusChangeActionType.mismatch", "StatusChangeActionType MisMatch with "+ message ,"");
	}

	
}