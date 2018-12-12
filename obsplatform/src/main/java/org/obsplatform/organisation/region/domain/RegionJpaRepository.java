package org.obsplatform.organisation.region.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegionJpaRepository extends JpaRepository<RegionMaster, Long>, JpaSpecificationExecutor<RegionMaster>{

}


