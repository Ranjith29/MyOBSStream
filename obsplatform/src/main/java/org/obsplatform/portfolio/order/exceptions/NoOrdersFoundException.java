package org.obsplatform.portfolio.order.exceptions;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class NoOrdersFoundException extends AbstractPlatformDomainRuleException {

    public NoOrdersFoundException() {
        super("error.msg.billing.order.not.found", "order not found ");
    }
    
    public NoOrdersFoundException(String msg) {
        super("error.msg.no.bills.to.generate", " No Bills TO Generate ", msg);
    }

	public NoOrdersFoundException(Long orderId) {
		 super("error.msg.orders.are.does.not.exist", "No Orders are does not exist with this "+orderId,orderId);
		 
	}
	
	public NoOrdersFoundException(Long clientId,Long planId) {
		super("error.msg.orders.not.found", "No Orders found with clientId "+clientId+" and planId "+planId,clientId);
		
	}
	
	public NoOrdersFoundException(String clientId, Long orderNo) {
		
		super("error.msg.orders.not.found", "No Orders found with clientId "+clientId+" and orderNo "+orderNo,clientId);
	}
	
	public NoOrdersFoundException(Long orderId,String clientId) {
		super("error.msg.asssociation.does.not.exit", "No association found for this Order"+orderId,orderId);
	}
}
