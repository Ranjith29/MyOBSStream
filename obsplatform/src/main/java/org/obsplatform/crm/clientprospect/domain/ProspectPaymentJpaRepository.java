package org.obsplatform.crm.clientprospect.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProspectPaymentJpaRepository extends
		JpaRepository<ProspectPayment, Long>,
		JpaSpecificationExecutor<ProspectPayment> {
	
	@Query("from ProspectPayment pPay where pPay.clientId=:clientId")
	ProspectPayment findOneProspectPaymentByClientId(@Param("clientId")Long clientId);

}


