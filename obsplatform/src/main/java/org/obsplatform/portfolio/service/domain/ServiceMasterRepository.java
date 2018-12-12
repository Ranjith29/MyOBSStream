package org.obsplatform.portfolio.service.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Long>,
JpaSpecificationExecutor<ServiceMaster>{
	
	@Query("from ServiceMaster service where service.serviceCode =:serviceCode")
	ServiceMaster findOneByServiceCode(@Param("serviceCode") String serviceCode);

}
