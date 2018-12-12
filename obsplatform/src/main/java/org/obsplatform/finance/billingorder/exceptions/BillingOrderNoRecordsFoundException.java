package org.obsplatform.finance.billingorder.exceptions;

import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.obsplatform.portfolio.client.domain.Client;


public class BillingOrderNoRecordsFoundException extends AbstractPlatformDomainRuleException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BillingOrderNoRecordsFoundException() {
        super("error.msg.billing.order.not.found", " Billing order not found ");
    }
    
    public BillingOrderNoRecordsFoundException(String msg) {
        super("error.msg.no.bills.to.generate", " No Bills TO Generate ", msg);
    }

	public BillingOrderNoRecordsFoundException(Long planCode) {
		 super("error.msg.no.active.price.available.for.this.plan", "No Active Prices Available For This Plan ", planCode);
		 
	}
	
	public BillingOrderNoRecordsFoundException(final String msg, final Long billId) {
		 super("error.msg.no.generate.pdf.file.for.this.statement", "No Generate Pdf File For This Statement ", billId);
		 
	}

	public BillingOrderNoRecordsFoundException(String msg, Client client) {
		 super("error.msg.no.emailId.is.available", "EmailId is not available",msg);
	}

	public BillingOrderNoRecordsFoundException(Invoice invoice) {
		 super("error.msg.no.invoice.is.not.available", "Invoice is not available for this Device",invoice);
	}
	
}
