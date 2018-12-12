package org.obsplatform.finance.paymentsgateway.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentGatewayConfigurationWritePlatformService {

	CommandProcessingResult updatePaymentGatewayConfig(Long configId, JsonCommand command);

	CommandProcessingResult createPaymentGatewayConfig(JsonCommand command);
	
}
