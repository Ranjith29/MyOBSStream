package org.obsplatform.logistics.supplier.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.supplier.data.SupplierData;

public interface SupplierReadPlatformService {
	
	public List<SupplierData> retrieveSupplier();

	public Page<SupplierData> retrieveSupplier(SearchSqlQuery searchSupplier);

	public List<SupplierData> retrieveSupplier(Long supplierId);

}
