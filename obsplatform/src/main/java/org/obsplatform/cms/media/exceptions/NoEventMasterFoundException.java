package org.obsplatform.cms.media.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class NoEventMasterFoundException extends AbstractPlatformDomainRuleException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoEventMasterFoundException() {
        super("error.msg.movie.not.found", "Event With this id does not exist");
    }
    
   
}
