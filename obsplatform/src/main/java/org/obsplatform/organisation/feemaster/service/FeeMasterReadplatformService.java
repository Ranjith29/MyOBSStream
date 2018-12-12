package org.obsplatform.organisation.feemaster.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.organisation.feemaster.data.FeeMasterData;

public interface FeeMasterReadplatformService {
	
	FeeMasterData retrieveSingleFeeMasterDetails(Long id);
	
	List<FeeMasterData> retrieveRegionPrice(Long id);
	
	Collection<FeeMasterData> retrieveAllData(String transType);

	FeeMasterData retrieveCustomerRegionClientTypeWiseFeeDetails(Long clientId, String codeRegistrationFee, Long categoryId);

}
