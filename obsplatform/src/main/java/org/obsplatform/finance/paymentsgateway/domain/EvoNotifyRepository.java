package org.obsplatform.finance.paymentsgateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EvoNotifyRepository extends JpaRepository<EvoNotify, Long>,JpaSpecificationExecutor<EvoNotify> {

}

