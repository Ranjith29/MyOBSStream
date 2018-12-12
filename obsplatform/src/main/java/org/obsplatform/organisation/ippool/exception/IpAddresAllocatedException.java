package org.obsplatform.organisation.ippool.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
public class IpAddresAllocatedException extends AbstractPlatformDomainRuleException {

  
    public IpAddresAllocatedException(final String msg) {
        super("error.msg.ipaddress.already.allocated.please.select.another.iprange", "Ipaddress already allocated please select another iprange", msg);
    }
	
}
