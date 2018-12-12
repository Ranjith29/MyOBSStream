package org.obsplatform.finance.paymentsgateway.domain;

import org.obsplatform.finance.paymentsgateway.exception.PaymentGatewayConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * <p>
 * Wrapper for {@link PaymentGatewayConfigurationRepository} .
 * </p>
 * @author ashokreddy
 */
@Service
public class PaymentGatewayConfigurationRepositoryWrapper {

	private final PaymentGatewayConfigurationRepository repository;
	
	@Autowired
	public PaymentGatewayConfigurationRepositoryWrapper(final PaymentGatewayConfigurationRepository repository) {
		this.repository = repository;
	}
	
	public String getValue(final String gatewayName) {
		
		final PaymentGatewayConfiguration pgConfig = findOneByNameWithEnableCheck(gatewayName);

		if (null != pgConfig.getValue()) {
			return pgConfig.getValue();
		}

		throw new PaymentGatewayConfigurationException(gatewayName);
	}

	public PaymentGatewayConfiguration findOneByName(final String gatewayName) {

		final PaymentGatewayConfiguration pgConfig = this.repository.findOneByName(gatewayName);

		if (null != pgConfig) {
			return pgConfig;
		}

		throw new PaymentGatewayConfigurationException(gatewayName);
	}
	
	public PaymentGatewayConfiguration findOneByNameWithEnableCheck(final String gatewayName) {
		
		final PaymentGatewayConfiguration pgConfig = findOneByName(gatewayName);
		
		if(pgConfig.isEnabled()) {
			return pgConfig;
		}
		
		throw new PaymentGatewayConfigurationException(gatewayName);
	}
}
