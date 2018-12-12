package org.obsplatform.logistics.itemdetails.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class SerialNumberAlreadyExistException extends AbstractPlatformDomainRuleException {

	public SerialNumberAlreadyExistException(String SerialNumber) {		
		super("error.msg.itemdetails.serialnumber.already.exist", "SerialNumber Already Exist "+SerialNumber, SerialNumber);
	}
}
