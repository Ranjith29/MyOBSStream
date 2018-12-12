
package org.obsplatform.finance.usagecharges.service;

import java.util.List;

import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;

/**
 * @author Ranjith
 *
 */
public interface UsageChargesWritePlatformService {

	CommandProcessingResult createUsageChargesRawData(JsonCommand command);

	void processCustomerUsageRawData(UsageChargesData customerData);

	BillingOrderCommand checkOrderUsageCharges(BillingOrderData billingOrderData);

	void updateUsageCharges(List<BillingOrderCommand> billingOrderCommands,Invoice singleInvoice);

}
