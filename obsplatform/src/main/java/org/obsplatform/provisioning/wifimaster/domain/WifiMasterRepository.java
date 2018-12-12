package org.obsplatform.provisioning.wifimaster.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WifiMasterRepository extends
		JpaRepository<WifiMaster, Long>, JpaSpecificationExecutor<WifiMaster> {
	
	@Query("from WifiMaster wd where wd.orderId =:orderId AND wd.clientId =:clientId AND  wd.is_deleted = 'N'")
	WifiMaster findOneByOrderId(@Param("clientId") final Long clientId,@Param("orderId") final Long orderId);
	

}
