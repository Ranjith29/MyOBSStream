package org.obsplatform.logistics.agent.service;

import java.util.List;

import org.obsplatform.logistics.agent.data.AgentItemSaleData;
import org.obsplatform.logistics.mrn.data.MRNDetailsData;

public interface ItemSaleReadPlatformService {

	List<AgentItemSaleData> retrieveAllData();

	AgentItemSaleData retrieveSingleItemSaleData(Long id);
	
	List<MRNDetailsData> retriveItemsaleIds();

}
