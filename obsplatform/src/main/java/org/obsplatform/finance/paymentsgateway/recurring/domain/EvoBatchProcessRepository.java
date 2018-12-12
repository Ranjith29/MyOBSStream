package org.obsplatform.finance.paymentsgateway.recurring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvoBatchProcessRepository extends JpaRepository<EvoBatchProcess, Long>, JpaSpecificationExecutor<EvoBatchProcess> {
	
	@Query("from EvoBatchProcess evoBatchProcess where evoBatchProcess.inputFileName =:inputFileName and evoBatchProcess.uploaded = 'Y'")
	EvoBatchProcess findOneByFileName(@Param("inputFileName") String inputFileName);

}
