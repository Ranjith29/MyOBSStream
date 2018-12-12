/**
 * 
 */
package org.obsplatform.cms.eventprice.service;

import java.util.List;

import org.obsplatform.cms.eventprice.data.ClientTypeData;
import org.obsplatform.cms.eventprice.data.EventPriceData;

/**
 * Interface for {@link EventPricing} Read Service
 * 
 * @author pavani
 *
 */
public interface EventPriceReadPlatformService {

	
	/**
	 * Method for retrieving {@link EventPricing} {@link List}
	 * 
	 * @param eventId
	 * @return
	 */
	List<EventPriceData> retrieventPriceData(Long eventId);
	
	/**
	 * Method for Retrieving {@link ClientTypeData}
	 * 
	 * @return
	 */
	List<ClientTypeData> clientType();
	
	/**
	 * Method for retrieving single {@link EventPricing}
	 * 
	 * @param eventPriceId
	 * @return
	 */
	EventPriceData  retrieventPriceDetails(Long eventPriceId);

}
