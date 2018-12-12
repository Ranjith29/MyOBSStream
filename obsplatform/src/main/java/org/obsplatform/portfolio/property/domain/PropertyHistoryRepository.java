package org.obsplatform.portfolio.property.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PropertyHistoryRepository extends JpaRepository<PropertyTransactionHistory, Long>,
JpaSpecificationExecutor<PropertyTransactionHistory>{

}
