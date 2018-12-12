package org.obsplatform.finance.paymentsgateway.service;

import org.obsplatform.infrastructure.configuration.data.ConfigurationData;
import org.obsplatform.infrastructure.configuration.data.ConfigurationPropertyData;

public interface PaymentGatewayConfigurationReadPlatformService {
	
	ConfigurationData retrievePaymentGatewayConfiguration();

	ConfigurationPropertyData retrievePaymentGatewayConfiguration(Long configId);

}
