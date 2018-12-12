package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.util.List;

import org.obsplatform.finance.paymentsgateway.recurring.data.EvoBatchProcessData;

public interface EvoRecurringBillingReadPlatformService {
	
	public List<EvoBatchProcessData> getUploadedFile();

}
