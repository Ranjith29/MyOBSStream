package org.obsplatform.finance.billingorder.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.domain.Invoice;

public interface GenerateReverseBillingOrderService {

	List<BillingOrderCommand> generateReverseBillingOrder(List<BillingOrderData> billingOrderProducts,LocalDate disconnectDate);

	Invoice generateNegativeInvoice(List<BillingOrderCommand> billingOrderCommands);

}
