package org.obsplatform.finance.depositandrefund.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepositAndRefundRepository extends JpaRepository<DepositAndRefund, Long>, JpaSpecificationExecutor<DepositAndRefund>{

	
	@Query("from DepositAndRefund deposit where deposit.billId =:billId and isRefund ='N' and deposit.paymentId IS NULL")
	List<DepositAndRefund> findListofDepositsIncludedInStatement(@Param("billId") Long billId);

	
}



