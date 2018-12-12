package org.obsplatform.finance.adjustment.service;

import java.util.List;

import org.obsplatform.finance.adjustment.data.AdjustmentData;
import org.obsplatform.finance.clientbalance.data.ClientBalanceData;

public interface AdjustmentReadPlatformService {
	
	List<ClientBalanceData> retrieveAllAdjustments(Long id);

	List<AdjustmentData> retrieveAllAdjustmentsCodes();

	
}
