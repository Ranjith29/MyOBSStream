package org.obsplatform.billing.chargecode.service;

import java.math.BigDecimal;

import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * @author hugo
 *
 */
public interface ChargeCodeWritePlatformService {

	 CommandProcessingResult createChargeCode(JsonCommand command);
	
	 CommandProcessingResult updateChargeCode(JsonCommand command, Long chargeCodeId);
	
	 BigDecimal calculateFinalAmount(ChargeCodeData chargeCodeData, Long clientId, Long priceId);
}
