package org.obsplatform.finance.billingorder.service;


import java.util.List;

import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.domain.Invoice;

public interface GenerateBillingOrderService {

	List<BillingOrderCommand> generateBillingOrder(List<BillingOrderData> products);

	Invoice generateInvoice(List<BillingOrderCommand> billingOrderCommands);

	Invoice generateMultiOrderInvoice(List<BillingOrderCommand> billingOrderCommands, Invoice invoice);
	
}
