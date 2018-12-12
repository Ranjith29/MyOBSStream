package org.obsplatform.cms.eventorder.service;

import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.plan.domain.Plan;

public interface PrepareRequestWriteplatformService {

	CommandProcessingResult prepareNewRequest(Order order,Plan plan, String requstStatus);

	void prepareRequestForRegistration(Long id, String action,String provisioningSystem);



}
