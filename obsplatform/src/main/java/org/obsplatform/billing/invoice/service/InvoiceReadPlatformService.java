
package org.obsplatform.billing.invoice.service;

import java.util.List;

import org.obsplatform.billing.invoice.data.InvoiceData;


public interface InvoiceReadPlatformService {

	List<InvoiceData> retrieveInvoiceDetails(Long id);

	List<InvoiceData> retrieveDueAmountInvoiceDetails(Long clientId);

	List<InvoiceData> retrieveExpiryOrderList();

}
