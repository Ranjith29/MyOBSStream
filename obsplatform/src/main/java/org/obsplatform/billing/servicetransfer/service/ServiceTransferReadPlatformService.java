package org.obsplatform.billing.servicetransfer.service;

import java.util.List;

import org.obsplatform.organisation.feemaster.data.FeeMasterData;

public interface ServiceTransferReadPlatformService {
	
	List<FeeMasterData> retrieveSingleFeeDetails(Long clientId, String transationType, Long planId, String contractPeriod);

	List<FeeMasterData> retrieveSingleFeeDetailsforclientZero(Long clientId,String string, Long planId, String contract, String state,
			String country);
}
