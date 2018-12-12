package org.obsplatform.portfolio.servicepartnermapping.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 
 * @author Naresh
 * 
 */
public interface ServicePartnerMappingRepository extends JpaRepository<ServicePartnerMapping, Long>,
		JpaSpecificationExecutor<ServicePartnerMapping> {

}
