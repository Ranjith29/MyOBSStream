package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


@SuppressWarnings("serial")
public class SchedulerOrderFoundException extends AbstractPlatformDomainRuleException {

    public SchedulerOrderFoundException(Long activeorderId) {
        super("error.msg.billing.order.startdate.greater.than.exist.orderendate ", "Order Start Date must be greater than exist order end date",activeorderId);
    }
    
   
}
