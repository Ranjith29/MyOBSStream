package org.obsplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface BillingOrderWritePlatformService {

	
	CommandProcessingResult updateBillingOrder(List<BillingOrderCommand> billingOrderCommands);
	
	void updateClientBalance(BigDecimal value,Long clientId, boolean isWalletEnable);
	
	void updateClientVoucherBalance(BigDecimal amount,Long clientId, boolean isWalletEnable);

	void UpdateOfficeCommision(Invoice invoice, Long agreementId);
	
}
