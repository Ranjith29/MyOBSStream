package org.obsplatform.portfolio.contract.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.portfolio.contract.data.PeriodData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;

public interface ContractPeriodReadPlatformService {

	Collection<SubscriptionData> retrieveAllSubscription();

	Collection<SubscriptionData> retrieveSubscriptionDetails();

	SubscriptionData retrieveSubscriptionData(Long subscriptionId);

	List<PeriodData> retrieveAllPlatformPeriod();

	List<SubscriptionData> retrieveSubscriptionDatabyContractType(String string, int i);

	List<SubscriptionData> retrieveSubscriptionDatabyOrder(Long orderId);


}
