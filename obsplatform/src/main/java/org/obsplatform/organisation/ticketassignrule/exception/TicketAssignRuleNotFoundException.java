package org.obsplatform.organisation.ticketassignrule.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class TicketAssignRuleNotFoundException extends AbstractPlatformResourceNotFoundException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TicketAssignRuleNotFoundException(final Long businessprocessId,final Long clientcategoryId) {
		super("error.msg.ticket.assign.rule.not.found", "TicketAssignRule with BusinessProcess & ClientCategoryType does not exist", businessprocessId);
	}
	
	public TicketAssignRuleNotFoundException(final Long id) {
		super("error.msg.ticket.assign.rule.id.invalid", "TicketAssignRule with identifier " + id + " does not exist", id);
	}

}
