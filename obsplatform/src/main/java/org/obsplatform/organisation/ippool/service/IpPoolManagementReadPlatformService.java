package org.obsplatform.organisation.ippool.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.ippool.data.IpPoolData;
import org.obsplatform.organisation.ippool.data.IpPoolManagementData;

/**
 * 
 * @author ashokreddy
 *
 */
public interface IpPoolManagementReadPlatformService {

	List<IpPoolData> getUnallocatedIpAddressDetailds();

	Long checkIpAddress(String ipaddress);

	Page<IpPoolManagementData> retrieveIpPoolData(SearchSqlQuery searchItemDetails, String type, String[] data);

	List<String> retrieveIpPoolIDArray(String query);

	IpPoolManagementData retrieveIpaddressData(String ipAddress);

	List<IpPoolManagementData> retrieveClientIpPoolDetails(Long clientId);

	IpPoolManagementData retrieveIdByIpAddress(String ip);

	List<IpPoolManagementData> retrieveSingleIpPoolDetails(Long poolId);


}
