package org.obsplatform.cms.epgprogramguide.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EpgProgramGuideJpaRepository extends JpaRepository<EpgProgramGuide, Long>,JpaSpecificationExecutor<EpgProgramGuide> {

}
