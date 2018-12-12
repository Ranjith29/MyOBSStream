package org.obsplatform.billing.promotioncodes.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a code is not found.
 */
public class PromotionCodeNotFoundException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	public PromotionCodeNotFoundException(final String id) {
		super("error.msg.promotionCode.not.found", "PromotionCode with this id" + id + "not exist", id);

	}

}