package org.obsplatform.logistics.mrn.domain;

import org.obsplatform.logistics.mrn.domain.MRNDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MRNDetailsJpaRepository extends JpaRepository<MRNDetails, Long>,
		JpaSpecificationExecutor<MRNDetails> {

}
