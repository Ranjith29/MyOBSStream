package org.obsplatform.finance.billingorder.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BillingOrderRepository extends  JpaRepository<BillingOrder, Long>,
JpaSpecificationExecutor<BillingOrder>{

}
