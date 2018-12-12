package org.obsplatform.portfolio.plan.service;

import java.util.List;

import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.organisation.partner.data.PartnersData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.data.ServiceData;

public interface PlanReadPlatformService {
	
	
	List<PlanData> retrievePlanData(String planType);
	
	List<SubscriptionData> retrieveSubscriptionData(Long orderId, String planType);
	
	List<EnumOptionData> retrieveNewStatus();
	
	PlanData retrievePlanData(Long planCode);
	
	List<ServiceData> retrieveSelectedServices(Long planId);
	
	List<EnumOptionData> retrieveVolumeTypes();

	List<PartnersData> retrievePartnersData(Long planId);

	List<PartnersData> retrieveAvailablePartnersData(Long planId);
	
	List<PlanData> retrieveAllPlanDetails();

	List<EventMasterData> retrieveSelectedEvents(Long planId);
	
	List<PlanData> retrieveSelectedClientCategorys(Long planId);

	List<PlanData> retrieveClientCategories();

	List<PlanData> findTalkPlanAddons(Long planId);


}
