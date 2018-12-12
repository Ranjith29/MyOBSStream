package org.obsplatform.organisation.feemaster.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class FeeMasterNotFoundException extends AbstractPlatformResourceNotFoundException {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeeMasterNotFoundException(String id) {
	
				super("error.msg.fee.master.id.not.found", "Fee Master Id "+id+" not found. ",id);
				}
	
	}