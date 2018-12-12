package org.obsplatform.cms.mediadevice.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DeviceDetailsInActiveException extends AbstractPlatformDomainRuleException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceDetailsInActiveException(final String msg){
		super("error.msg.mediadevice.devices.active", "some Media device are active, please inactive those media devices",msg);
	}
}
