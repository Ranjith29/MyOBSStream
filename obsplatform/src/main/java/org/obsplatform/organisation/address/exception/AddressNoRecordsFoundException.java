package org.obsplatform.organisation.address.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AddressNoRecordsFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddressNoRecordsFoundException(String entity) {
		super("error.msg.billing.address."+entity+".not.found", "City Not Found");
	}

}
