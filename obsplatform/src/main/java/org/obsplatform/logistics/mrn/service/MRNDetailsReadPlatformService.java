package org.obsplatform.logistics.mrn.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.mrn.data.InventoryTransactionHistoryData;
import org.obsplatform.logistics.mrn.data.MRNDetailsData;

public interface MRNDetailsReadPlatformService {

	List<MRNDetailsData> retriveMRNDetails();

	Collection<MRNDetailsData> retriveMrnIds();

	List<String> retriveSerialNumbers(Long fromOffice);

	Page<InventoryTransactionHistoryData> retriveHistory(SearchSqlQuery searchItemHistory);
	
	 MRNDetailsData retriveSingleMrnDetail(Long mrnId);

	 Page<MRNDetailsData> retriveMRNDetails(SearchSqlQuery searchMRNDetails);

	InventoryTransactionHistoryData retriveSingleMovedMrn(Long mrnId);

	List<String> retriveSerialNumbersForItems(Long officeId, String serialNumber);
	
	//For Mrn Track History
	List<InventoryTransactionHistoryData> retriveMrnTrackDetails(Long mrnId);

}
