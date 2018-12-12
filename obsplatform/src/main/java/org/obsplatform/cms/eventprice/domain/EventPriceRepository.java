/**
 * 
 */
package org.obsplatform.cms.eventprice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Interface for {@link EventPricing} Repository
 * extends {@link JpaRepository} and {@link JpaSpecificationExecutor}
 * 
 * @author pavani
 *
 */
public interface EventPriceRepository extends
		JpaRepository<EventPrice, Long>,
		JpaSpecificationExecutor<EventPrice> {

}
