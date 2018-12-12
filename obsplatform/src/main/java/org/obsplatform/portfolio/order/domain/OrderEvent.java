package org.obsplatform.portfolio.order.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "b_order_events")
public class OrderEvent extends AbstractAuditableCustom<AppUser, Long>  {

    @ManyToOne
    @JoinColumn(name="order_id")
	private Order orders;

	@Column(name = "event_id")
	private Long eventId;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "event_status")
	private Long eventStatus;

	@Column(name = "is_deleted")
	private char isDeleted;

	public OrderEvent()
	{}

	public OrderEvent(Long eventId, String eventType, Long eventStatus, char isDeleted) {
		
		this.eventId = eventId;
		this.eventType = eventType;
		this.eventStatus = eventStatus;
		this.isDeleted = isDeleted;
	}

	public  void update(Order order)
	{
		this.orders=order;

	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	
	
	
}
