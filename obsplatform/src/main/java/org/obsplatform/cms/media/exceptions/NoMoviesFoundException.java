package org.obsplatform.cms.media.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class NoMoviesFoundException extends AbstractPlatformDomainRuleException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMoviesFoundException() {
        super("error.msg.movie.not.found", " Movie Format Does Not Exist ");
    }
    
   
}
