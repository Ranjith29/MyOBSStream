package org.obsplatform.provisioning.entitlements.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CubiwareRequiredDataNotFoundException extends AbstractPlatformDomainRuleException {

	public CubiwareRequiredDataNotFoundException(Long clientId, String parameter) {
		super("error.msg.cubiware." + parameter + ".not.exist", parameter + " value does not exist and clientId=" + clientId, clientId);   
	}

}
