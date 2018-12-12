package org.obsplatform.finance.paymentsgateway.recurring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecurringBillingHistoryRepository
		extends JpaRepository<RecurringBillingHistory, Long>, JpaSpecificationExecutor<RecurringBillingHistory> {

}
