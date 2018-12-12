package org.obsplatform.organisation.hardwareplanmapping.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ItemCodeDuplicateException extends
		AbstractPlatformDomainRuleException {

	public ItemCodeDuplicateException(String msg) {
		super("error.msg.item.code.already.configured",
				"Item Code already Configured with this plan", msg);
	}

}
