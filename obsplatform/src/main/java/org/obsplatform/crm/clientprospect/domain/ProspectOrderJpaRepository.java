package org.obsplatform.crm.clientprospect.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProspectOrderJpaRepository extends
		JpaRepository<ProspectOrder, Long>,
		JpaSpecificationExecutor<ProspectOrder> {
	
	@Query("from ProspectOrder pOrder where pOrder.prospectId=:prospectId")
	ProspectOrder findOneProspectOrderByProspectId(@Param("prospectId")Long prospectId);

}

