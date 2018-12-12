package org.obsplatform.finance.payments.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
JpaSpecificationExecutor<Payment> {
	
	@Query("from Payment payments where payments.refernceId=:refernceId and payments.deleted= '0' ")
	List<Payment> findOneByRefId(@Param("refernceId") Long refernceId);

}
