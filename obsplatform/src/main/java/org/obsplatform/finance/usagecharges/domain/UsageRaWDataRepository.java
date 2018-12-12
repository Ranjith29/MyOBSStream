
package org.obsplatform.finance.usagecharges.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Ranjith
 *
 */
public interface UsageRaWDataRepository extends JpaRepository<UsageRaw, Long>,JpaSpecificationExecutor<UsageRaw> {
	
	
	/*@Query("from UsageRaw usageRaw where usageRaw.clientId =:clientId and usageRaw.number =:number and usageRaw.usageCharge IS NULL")
	List<UsageRaw> findUsageRawDataByCustomerDetails(@Param("clientId") Long clientId, @Param("number") String number);*/
	
	@Query(value="select * from b_usage_raw usageRaw where usageRaw.client_id = ?1 and usageRaw.number = ?2 and usageRaw.usage_charge_id IS NULL "
			+ " and DATE_FORMAT(usageRaw.time,'%Y-%M')= DATE_FORMAT((CURRENT_DATE - INTERVAL 1 DAY),'%Y-%M') ",nativeQuery = true)
	List<UsageRaw> findUsageRawDataByCustomerDetails(Long clientId, String number);
	

}
