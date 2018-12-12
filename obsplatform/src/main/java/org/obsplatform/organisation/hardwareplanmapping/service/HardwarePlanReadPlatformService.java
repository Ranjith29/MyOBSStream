package org.obsplatform.organisation.hardwareplanmapping.service;

import java.util.List;

import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.hardwareplanmapping.data.HardwareMappingDetailsData;
import org.obsplatform.organisation.hardwareplanmapping.data.HardwarePlanData;
import org.obsplatform.portfolio.plan.data.PlanCodeData;

public interface HardwarePlanReadPlatformService {

	List<HardwarePlanData> retrievePlanData(String itemCode);

	List<ItemData> retrieveItems();

	List<PlanCodeData> retrievePlans();

	HardwarePlanData retrieveSinglePlanData(Long planId);

	List<HardwarePlanData> retrieveItems(String itemCode);

	List<HardwareMappingDetailsData> getPlanDetailsByItemCode(String itemCode,
			Long clientId);
	
	List<HardwarePlanData> retrieveRulebyPlanCodeandItemCode(String itemCode,String  planCode);
	
}
