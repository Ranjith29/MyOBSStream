/**
 * 
 */
package org.obsplatform.cms.eventmaster.data;

import org.joda.time.LocalDate;
import org.obsplatform.cms.eventmaster.domain.EventDetails;

/**
 * POJO for {@link EventDetails}
 * 
 * @author Pavani
 * @author Rakesh
 */
public class EventDetailsData {
	
	private Long id;
	private String eventName;
	private Integer eventId;
	private Long mediaId;	
	private LocalDate eventStartDate;
	private LocalDate eventEndDate;	
	private String mediaTitle; 			
	
	/**
     * <p> The behavior of this constructor when the given @param's are called
     * 	it sets to that particular @param varibles
     *
     * @param  id
     * @param  eventId    
     * @param  mediaId 
     * @param  eventStartDate    
     * @param  eventEndDate 
     */
	public EventDetailsData(final Long id, final Integer eventId, final Long mediaId, 
							final LocalDate eventStartDate, final LocalDate eventEndDate) {
		this.id = id;
		this.eventId = eventId;
		this.mediaId = mediaId;
		this.eventStartDate = eventStartDate;
		this.eventEndDate = eventEndDate;
	}
	
	/**
     * <p> The behavior of this constructor when the given params are called
     * 	it sets to that particular @param varibles
     *
     * @param  id
     * @param  eventId    
     * @param  mediaId 
     * @param  mediaTitle  
     * @param  eventName  
     *
     */
	public EventDetailsData(final Long id, final Integer eventId, final Long mediaId, final String mediaTitle, final String eventName) {
		this.id = id;
		this.eventId = eventId;
		this.mediaId = mediaId;
		this.mediaTitle = mediaTitle;
		this.eventName = eventName;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}
	/**
	 * @return the eventId
	 */
	public Integer getEventId() {
		return eventId;
	}
	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(final Integer eventId) {
		this.eventId = eventId;
	}
	/**
	 * @return the mediaId
	 */
	public Long getMediaId() {
		return mediaId;
	}
	/**
	 * @param mediaId the mediaId to set
	 */
	public void setMediaId(final Long mediaId) {
		this.mediaId = mediaId;
	}
	/**
	 * @return the eventStartDate
	 */
	public LocalDate getEventStartDate() {
		return eventStartDate;
	}
	/**
	 * @param eventStartDate the eventStartDate to set
	 */
	public void setEventStartDate(final LocalDate eventStartDate) {
		this.eventStartDate = eventStartDate;
	}
	/**
	 * @return the eventEndDate
	 */
	public LocalDate getEventEndDate() {
		return eventEndDate;
	}
	/**
	 * @param eventEndDate the eventEndDate to set
	 */
	public void setEventEndDate(final LocalDate eventEndDate) {
		this.eventEndDate = eventEndDate;
	}

	/**
	 * @return the mediaTitle
	 */
	public String getMediaTitle() {
		return mediaTitle;
	}

	/**
	 * @param mediaTitle the mediaTitle to set
	 */
	public void setMediaTitle(final String mediaTitle) {
		this.mediaTitle = mediaTitle;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}
