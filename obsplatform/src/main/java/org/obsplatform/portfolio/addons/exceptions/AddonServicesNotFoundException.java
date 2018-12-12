package org.obsplatform.portfolio.addons.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AddonServicesNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddonServicesNotFoundException(final Long orderId) {
		super("error.msg.addons.not.found.with.this.identifier","Addons not found with this identifier",orderId);
		
	}

}
