package org.obsplatform.crm.clientprospect.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProspectCardDetailsJpaRepository extends
		JpaRepository<ProspectCardDetails, Long>,
		JpaSpecificationExecutor<ProspectCardDetails> {

	
	@Query("from ProspectCardDetails pCardDetails where pCardDetails.prospectId=:prospectId")
	ProspectCardDetails findOneProspectCardDetailsByProspectID(@Param("prospectId")Long prospectId);
	
}
