
package org.obsplatform.finance.usagecharges.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author Ranjith
 *
 */
public interface UsageChargeRepository extends JpaRepository<UsageCharge, Long>,JpaSpecificationExecutor<UsageCharge> {

	
}
