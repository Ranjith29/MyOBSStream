
package org.obsplatform.finance.usagecharges.service;

import java.util.List;

import org.obsplatform.finance.usagecharges.data.UsageChargesData;

/**
 * @author Ranjith
 * 
 */
public interface UsageChargesReadPlatformService {

	List<UsageChargesData> retrieveOrderCdrData(Long clientId, Long orderId);

}
