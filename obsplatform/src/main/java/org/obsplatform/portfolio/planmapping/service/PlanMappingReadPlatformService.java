package org.obsplatform.portfolio.planmapping.service;

import java.util.List;

import org.obsplatform.portfolio.plan.data.PlanCodeData;
import org.obsplatform.portfolio.planmapping.data.PlanMappingData;

/**
 * 
 * @author ashokreddy
 *
 */
public interface PlanMappingReadPlatformService {

	List<PlanMappingData> getPlanMapping();

	List<PlanCodeData> getPlanCode();

	PlanMappingData getPlanMapping(Long planMappingId);
	
	

}
