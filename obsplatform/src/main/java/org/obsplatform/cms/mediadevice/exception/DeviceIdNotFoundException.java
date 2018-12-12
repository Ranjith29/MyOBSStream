package org.obsplatform.cms.mediadevice.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DeviceIdNotFoundException extends AbstractPlatformDomainRuleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceIdNotFoundException(final String id){
		super("error.msg.deviceId.not.found", "Device not found with this "+id,id);
	}
}
