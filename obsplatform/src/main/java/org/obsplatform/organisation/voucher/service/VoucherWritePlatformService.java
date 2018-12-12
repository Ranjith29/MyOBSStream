package org.obsplatform.organisation.voucher.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author ashokreddy
 * @author rakesh
 *
 */
public interface VoucherWritePlatformService {

	CommandProcessingResult createRandomGenerator(JsonCommand command);

	CommandProcessingResult generateVoucherPinKeys(Long batchId);

	CommandProcessingResult updateUpdateVoucherPins(Long entityId, JsonCommand command);

	CommandProcessingResult deleteUpdateVoucherPins(Long entityId, JsonCommand command);

	CommandProcessingResult cancelVoucherPins(Long entityId,JsonCommand command);
	

	
}
