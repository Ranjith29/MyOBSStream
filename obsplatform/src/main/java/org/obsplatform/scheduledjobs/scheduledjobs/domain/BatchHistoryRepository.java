package org.obsplatform.scheduledjobs.scheduledjobs.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BatchHistoryRepository extends JpaRepository<BatchHistory, Long>,
JpaSpecificationExecutor<BatchHistory>{

}
