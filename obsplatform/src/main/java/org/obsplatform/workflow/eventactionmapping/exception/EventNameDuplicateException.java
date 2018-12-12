package org.obsplatform.workflow.eventactionmapping.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class EventNameDuplicateException extends AbstractPlatformDomainRuleException {

	public EventNameDuplicateException(String msg) {
		super("error.msg.event.name.already.configured","Event Name already Configured with this Action Name", msg);
	}

}
