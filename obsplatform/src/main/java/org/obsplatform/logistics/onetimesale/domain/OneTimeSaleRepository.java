package org.obsplatform.logistics.onetimesale.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OneTimeSaleRepository extends
		JpaRepository<OneTimeSale, Long>, JpaSpecificationExecutor<OneTimeSale> {

}
