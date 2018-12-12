package org.obsplatform.logistics.itemdetails.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class ItemRegionalPriceNotFoundException extends AbstractPlatformDomainRuleException {

		public ItemRegionalPriceNotFoundException(String SerialNumber) {		
			super("error.msg.item.regionalprice.not.found.for.this.serialnumber", "No RegionalPrice Found for this SerialNumber "+SerialNumber, SerialNumber);
		}
}
