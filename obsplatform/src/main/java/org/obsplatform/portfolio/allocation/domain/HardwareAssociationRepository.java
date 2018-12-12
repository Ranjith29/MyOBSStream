package org.obsplatform.portfolio.allocation.domain;

import java.util.List;

import org.obsplatform.portfolio.association.domain.HardwareAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HardwareAssociationRepository  extends JpaRepository<HardwareAssociation, Long>,
JpaSpecificationExecutor<HardwareAssociation>{

	@Query("from HardwareAssociation association where association.serialNo =:serialNum and association.isDeleted='N'")
    List<HardwareAssociation> findOneByserialNo(@Param("serialNum") String serialNum);
	
	@Query("from HardwareAssociation association where association.orderId =:orderId and association.isDeleted='N'")
	List<HardwareAssociation> findOneByOrderId(@Param("orderId")Long orderId);
	
	@Query("from HardwareAssociation association where association.orderId =:orderId and association.clientId =:clientId and association.isDeleted='N'")
	List<HardwareAssociation> findOneByOrderAndClient(@Param("orderId")Long orderId,@Param("clientId")Long clientId);
	
	@Query("from HardwareAssociation association where association.orderId =:orderId and association.isDeleted='Y'")
	List<HardwareAssociation> findOneByOldOrderId(@Param("orderId")Long orderId);
	
}
