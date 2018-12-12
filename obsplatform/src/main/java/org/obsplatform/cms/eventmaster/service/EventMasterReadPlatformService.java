package org.obsplatform.cms.eventmaster.service;

import java.util.List;

import org.obsplatform.cms.eventmaster.data.EventDetailsData;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.cms.eventmaster.domain.EventDetails;
import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.cms.eventmaster.domain.OptType;
import org.obsplatform.infrastructure.core.data.EnumOptionData;

import ch.qos.logback.core.status.Status;

/**
 * Interface for {@link EventMaster} Read Service
 * 
 * @author pavani
 * @author Rakesh
 */
public interface EventMasterReadPlatformService {


	/**
	 * Method for retrieving {@link OptType} {@link EnumOptionData}
	 * 
	 * @return
	 */
	List<EnumOptionData> retrieveOptTypeData();

	/**
	 * Method for retrieving {@link Status} {@link EnumOptionData}
	 * 
	 * @return
	 */
	List<EnumOptionData> retrieveNewStatus();
	
	/**
	 * Method for retrieving {@link EventMaster} {@link List} 
	 * 
	 * @return
	 */
	List<EventMasterData> retrieveEventMasterData();
	
	/**
	 * Method for retrieving single {@link EventMaster}
	 * 
	 * @param eventId
	 * @return
	 */
	EventMasterData retrieveEventMasterDetails(Integer eventId);
	
	/**
	 * Method for retrieving single {@link EventDetails}
	 * 
	 * @param eventId
	 * @return
	 */
	EventDetailsData retrieveEventDetails(Integer eventId);
	
	/**
	 * Method for retrieving {@link EventDetails} {@link List}
	 * 
	 * @param eventId
	 * @return
	 */
	List<EventDetailsData> retrieveEventDetailsData(Integer eventId);
	
	/**
	 * Method for retrieving {@link EventDetails} {@link List}
	 * 
	 * @param eventId
	 * @return
	 */
	List<EventMasterData> retrieveEventMasterDataForEventOrders();
	
}
