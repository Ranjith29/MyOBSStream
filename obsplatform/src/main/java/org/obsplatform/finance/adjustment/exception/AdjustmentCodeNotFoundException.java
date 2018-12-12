package org.obsplatform.finance.adjustment.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AdjustmentCodeNotFoundException extends AbstractPlatformDomainRuleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdjustmentCodeNotFoundException(final String adjustmentCode) {
		super("error.msg.adjustment.code.invalid", "Invalid Adjustment Code "+adjustmentCode+". ",adjustmentCode);
	}
}
