package org.obsplatform.finance.paymentsgateway.recurring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringBillingRepository
		extends JpaRepository<RecurringBilling, Long>, JpaSpecificationExecutor<RecurringBilling> {

	@Query("from RecurringBilling recurringBilling where recurringBilling.deleted='N' and recurringBilling.subscriberId =:subscriberId")
	RecurringBilling findOneBySubscriberId(@Param("subscriberId") String subscriberId);

	@Query("from RecurringBilling recurringBilling where recurringBilling.deleted='N' and recurringBilling.orderId =:orderId")
	RecurringBilling findOneByOrderId(@Param("orderId") Long orderId);

	@Query("from RecurringBilling billing where billing.deleted='N' and billing.subscriberId =:subscriberId and billing.clientId =:clientId")
	RecurringBilling findOneByClientAndProfileId(@Param("subscriberId") String subscriberId,
			@Param("clientId") Long clientId);

}
