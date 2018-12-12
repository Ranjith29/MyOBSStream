package org.obsplatform.infrastructure.jobs.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class NoLogFileFoundException extends AbstractPlatformDomainRuleException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoLogFileFoundException() {
        super("error.msg.billing.Job.logfile .found", "Log Files are Not created ");
    }
    
   
}
