package org.obsplatform.portfolio.service.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.service.data.ServiceMasterData;
import org.obsplatform.portfolio.service.data.ServiceMasterOptionsData;

public interface ServiceMasterReadPlatformService {
	

	List<ServiceData> retrieveAllServices(String serviceType);
	
	 Collection<ServiceMasterOptionsData> retrieveAllServiceMasterOptionsData() ;

	Page<ServiceMasterData> retrieveServices(SearchSqlQuery searchCodes);

	ServiceMasterData retrieveIndividualService(Long serviceId);

	List<EnumOptionData> retrieveServicesTypes();

	List<EnumOptionData> retrieveServiceUnitType();
}
