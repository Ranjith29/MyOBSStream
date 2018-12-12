package org.obsplatform.provisioning.entitlements.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CubiwareFailureStatusCodeException extends AbstractPlatformDomainRuleException {

	public CubiwareFailureStatusCodeException(Long statusCode) {
		super("error.msg.cubiware.statuscode.failure", " Cubiware System Returns Error StatusCode:" + statusCode, statusCode);   
	}
}
