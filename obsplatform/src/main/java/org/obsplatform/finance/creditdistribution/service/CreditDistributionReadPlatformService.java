package org.obsplatform.finance.creditdistribution.service;

import java.util.List;

import org.obsplatform.finance.creditdistribution.data.CreditDistributionData;
import org.obsplatform.infrastructure.core.service.Page;

public interface CreditDistributionReadPlatformService {


	Page<CreditDistributionData> getClientDistributionData(Long clientId);

	List<CreditDistributionData> retrievePaymentId(Long paymentId);
}
