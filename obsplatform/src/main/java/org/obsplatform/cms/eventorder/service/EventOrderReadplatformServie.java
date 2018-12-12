package org.obsplatform.cms.eventorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.cms.eventorder.data.EventOrderData;
import org.obsplatform.cms.eventorder.data.EventOrderDeviceData;
import org.obsplatform.logistics.onetimesale.data.OneTimeSaleData;

public interface EventOrderReadplatformServie {
	
	List<OneTimeSaleData> retrieveEventOrderData(Long clientId);

	boolean CheckClientCustomalidation(Long clientId);

	List<EventOrderData> getTheClientEventOrders(Long clientId);

	List<EventOrderDeviceData> getDevices(Long clientId);

	List<EventMasterData> getEvents();

	BigDecimal retriveEventPrice(String fType, String oType, Long clientId);

	Long getCurrentRow(String fType, String oType, Long clientId);
	
	BigDecimal retriveEventPriceNew(String fType, String oType, Long clientId ,Long eventId);
	
}
