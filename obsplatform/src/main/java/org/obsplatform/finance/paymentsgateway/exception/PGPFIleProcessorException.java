package org.obsplatform.finance.paymentsgateway.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PGPFIleProcessorException extends AbstractPlatformDomainRuleException {

	public PGPFIleProcessorException(String value) {
		super("error.msg.pgp.security.details.not.found", value + " not Found in the home location",value);
	}
	
	public PGPFIleProcessorException() {
		super("error.msg.pgp.security.actionType.not.null",  "ActionType(Encrypt/Decryption) value not null", "");
	}
}
