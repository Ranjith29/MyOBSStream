package org.obsplatform.finance.payments.exception;

import java.math.BigDecimal;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PaymentDetailsNotFoundException extends AbstractPlatformDomainRuleException {


	private static final long serialVersionUID = -2726286660273906232L;
	public PaymentDetailsNotFoundException(final String paymentCode) {
		super("error.msg.payments.payment.details.invalid", "Payment Details Not Found"+paymentCode+". ",paymentCode);
	}
	
	public PaymentDetailsNotFoundException(final BigDecimal refundAmountPaid) {
		super("error.msg.refund.payment.amount.invalid", "Refund Amount lessthan or equals to Payment Amount"+refundAmountPaid+".",refundAmountPaid);
	}
}
