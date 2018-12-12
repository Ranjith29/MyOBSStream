package org.obsplatform.logistics.itemdetails.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SerialNumberNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SerialNumberNotFoundException(String SerialNumber) {		
		super("error.msg.itemdetails.serialnumber.not.found", "SerialNumber not Exist with this "+SerialNumber, SerialNumber);
	}
	
	public SerialNumberNotFoundException() {		
		super("error.msg.this.serialnumber.already.allocated.to.customer", "SerialNumber already allocated to customer");
	}

}
