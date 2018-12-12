package org.obsplatform.logistics.grn.service;

import java.util.Collection;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.itemdetails.data.InventoryGrnData;

public interface GrnReadPlatformService {

	public Collection<InventoryGrnData> retriveGrnDetails();
	
	InventoryGrnData retriveGrnDetailTemplate(Long grnId);
	
	public boolean validateForExist(final Long grnId);
	
	public Collection<InventoryGrnData> retriveGrnIds();
	
	public Page<InventoryGrnData> retriveGrnDetails(SearchSqlQuery searchGrn);
	
	public Collection<InventoryGrnData> retriveGrnIdswithItemId(final Long itemMasterId);
	
	
}
