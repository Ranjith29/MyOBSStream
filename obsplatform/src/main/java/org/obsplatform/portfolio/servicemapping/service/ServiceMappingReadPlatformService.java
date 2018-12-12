package org.obsplatform.portfolio.servicemapping.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.portfolio.servicemapping.data.ServiceCodeData;
import org.obsplatform.portfolio.servicemapping.data.ServiceMappingData;
import org.obsplatform.provisioning.provisioning.data.ServiceParameterData;

public interface ServiceMappingReadPlatformService {
	
	
	List<ServiceCodeData> getServiceCode();
	
	Page<ServiceMappingData> getServiceMapping(SearchSqlQuery searchCodes);

	ServiceMappingData getServiceMapping(Long serviceMappingId);

	List<ServiceParameterData> getSerivceParameters(Long orderId, Long serviceId);

	List<ServiceMappingData> retrieveOptionalServices(String string);

	List<ItemData> retrieveItems();
	
	List<ServiceMappingData> retrieveRuleServicewithMultipleItem(Long serviceId, Long itemId);
	

}
