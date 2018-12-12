package org.obsplatform.vendoragreement.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AgreementfileNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AgreementfileNotFoundException(Long fileId) {
		super("error.msg.file.not.found.with.this.identifier","file not found",fileId);
		
	}

}
