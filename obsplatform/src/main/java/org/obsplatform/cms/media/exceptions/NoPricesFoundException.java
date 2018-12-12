package org.obsplatform.cms.media.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class NoPricesFoundException extends AbstractPlatformDomainRuleException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoPricesFoundException() {
        super("error.msg.movie.price.not.found", " No Price Found for This Client Type");
    }
    
   
}
