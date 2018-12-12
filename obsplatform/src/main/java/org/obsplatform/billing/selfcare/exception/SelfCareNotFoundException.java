package org.obsplatform.billing.selfcare.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfCareNotFoundException extends AbstractPlatformDomainRuleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelfCareNotFoundException(final Long id){
		super("error.msg.clientId.not.found", "Client not found with this " + id, id);
	}
	
	
	

}
