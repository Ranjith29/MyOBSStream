package org.obsplatform.portfolio.servicepartnermapping.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.portfolio.servicepartnermapping.data.ServicePartnerMappingData;

/**
 * 
 * @author Naresh
 * 
 */
public interface ServicePartnerMappingReadPlatformService {

	Page<ServicePartnerMappingData> getAllServicePartnerMappingData(SearchSqlQuery searchCodes);

	List<ServicePartnerMappingData> getServiceCode();

	List<ServicePartnerMappingData> getPartnerNames();

	ServicePartnerMappingData getServicePtrMappingById(Long servicePtrMappId);

	List<ServicePartnerMappingData> getServiceCode(Long serviceId);

}
