package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.util.List;

import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringData;

public interface AuthorizeRecurringBillingProfileReadPlatformService {

	List<RecurringData> retrieveRecurringData(final Long clientId);
}
