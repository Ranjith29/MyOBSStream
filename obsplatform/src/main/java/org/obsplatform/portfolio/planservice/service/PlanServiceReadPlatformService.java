package org.obsplatform.portfolio.planservice.service;

import java.util.Collection;

import org.obsplatform.portfolio.planservice.data.PlanServiceData;

public interface PlanServiceReadPlatformService {

	Collection<PlanServiceData> retrieveClientPlanService(Long clientId,
			String serviceType,String category);

	Collection<PlanServiceData> retrieveClientPlanService(Long clientId,
			String serviceType, Boolean isCategoryOnly);

}
