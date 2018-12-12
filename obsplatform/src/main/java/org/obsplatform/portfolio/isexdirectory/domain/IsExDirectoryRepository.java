package org.obsplatform.portfolio.isexdirectory.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * @author Naresh
 *
 */
public interface IsExDirectoryRepository  extends JpaRepository<IsExDirectory, Long>, JpaSpecificationExecutor<IsExDirectory>{

	@Query("from IsExDirectory isExDirectory where isExDirectory.orderId=:orderId")
	IsExDirectory findOneByOrderId(@Param("orderId") Long orderId);

}
