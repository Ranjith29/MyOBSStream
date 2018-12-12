package org.obsplatform.logistics.itemdetails.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class OrderQuantityExceedsException extends AbstractPlatformDomainRuleException {

   
	private static final long serialVersionUID = 1L;


	public OrderQuantityExceedsException(final Long orderId) {
		 super("error.msg.order.quantity..exceeds", "No more order quantity for grn id "+orderId,orderId);
		 
	}
	
	public OrderQuantityExceedsException() {
		 super("error.msg.edit.items.assign.to.customer", "Items assigned to customer, unable to edit it");
		 
	}
	
	public OrderQuantityExceedsException(final String msg) {
		 super("error.msg.delete.items.assign.to.customer", "Items assigned to customer, unable to delete it"+msg);
		 
	}
	
	public OrderQuantityExceedsException(final String planDescription,final Long activeOrdersCount) {
		 super("error.msg.max.number.of.connections.exceeded.for.this.plan", "Max number of connections exceeded for this plan" +planDescription);
	}
}
