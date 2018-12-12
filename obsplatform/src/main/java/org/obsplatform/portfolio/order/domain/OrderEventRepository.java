package org.obsplatform.portfolio.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderEventRepository extends
JpaRepository<OrderEvent, Long>,
JpaSpecificationExecutor<OrderEvent>{

}
