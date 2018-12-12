package org.obsplatform.portfolio.plan.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository  extends JpaRepository<Plan, Long>,
JpaSpecificationExecutor<Plan>{

	@Query("from Plan plan where plan.id =:planId and is_deleted='N'")
	Plan findPlanCheckDeletedStatus(@Param("planId") Long planId);

}
