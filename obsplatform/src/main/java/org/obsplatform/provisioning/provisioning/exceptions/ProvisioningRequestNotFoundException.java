package org.obsplatform.provisioning.provisioning.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ProvisioningRequestNotFoundException extends AbstractPlatformDomainRuleException {

	public ProvisioningRequestNotFoundException(Long provisionId) {
		super("error.msg.provisioning.request.not.found.with.this.identifier","provisioning request not found with this identifier",provisionId);
		
	}

}
