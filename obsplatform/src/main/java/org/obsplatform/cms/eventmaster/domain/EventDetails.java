/**
 * 
 */
package org.obsplatform.cms.eventmaster.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Domain for {@link EventDetails}
 * 
 * @author pavani
 *
 */

@Entity
@Table(name = "b_mod_detail")
public class EventDetails extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "event_id")
	private EventMaster event;
	
	@Column(name = "media_id")
	private Long mediaId;
	
	@Column(name = "is_deleted")
	private char isDeleted = 'N';
	
	/**
	 * @param mediaId
	 * */
	public EventDetails(final Long mediaId) {
		this.mediaId = mediaId;
		this.event = null;
	}

	/**
	 * method
	 * @param event
	 * */
	public void update (final EventMaster event){
		this.event = event;
	}
	
	/**
	 * method
	 * @param event
	 * */
	public void delete(final EventMaster event) { 
		final LocalDate date = DateUtils.getLocalDateOfTenant();
		this.event = event;
		event.setEventEndDate(date.toDate());
	}
	
	/**
	 * Constructor
	 * @param event
	 * */
	public EventDetails(final EventMaster event) {
		this.event = event;
	}
	
	/** Default Constructor */
	public EventDetails() {
		
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
	 * @return the event
	 */
	public EventMaster getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(final EventMaster event) {
		this.event = event;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	
}
	