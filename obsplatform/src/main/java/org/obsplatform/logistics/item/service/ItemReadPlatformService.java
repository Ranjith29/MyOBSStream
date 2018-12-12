package org.obsplatform.logistics.item.service;

import java.util.List;

import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.item.data.ItemData;

public interface ItemReadPlatformService {

	List<EnumOptionData> retrieveItemClassType();

	List<EnumOptionData> retrieveUnitTypes();

	List<ChargesData> retrieveChargeCode();

	List<ItemData> retrieveAllItems();

	ItemData retrieveSingleItemDetails(Long clientId, Long itemId, String region, boolean isWithClientId);
	
	Page<ItemData> retrieveAllItems(SearchSqlQuery searchItems);

	List<ItemData> retrieveAuditDetails(Long itemId);

	List<ItemData> retrieveItemPrice(Long itemId);

}
