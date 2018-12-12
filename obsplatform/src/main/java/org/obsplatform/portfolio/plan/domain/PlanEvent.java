package org.obsplatform.portfolio.plan.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author rakesh
 *
 */
@Entity
@Table(name = "b_plan_events")
public class PlanEvent {

	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@ManyToOne
    @JoinColumn(name="plan_id")
    private Plan plan;

	@Column(name ="event_name")
    private Long eventId;


	@Column(name = "is_deleted", nullable = false)
	private char isDeleted = 'n';


	public PlanEvent()
	{
		  // This constructor is intentionally empty. Nothing special is needed here.
	}


	public PlanEvent(Long eventId) {
		this.eventId = eventId;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Plan getPlan() {
		return plan;
	}


	public void setPlan(Plan plan) {
		this.plan = plan;
	}


	public Long getEventId() {
		return eventId;
	}


	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}


	public char getIsDeleted() {
		return isDeleted;
	}


	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public void update(final Plan plan)
	{
		this.plan=plan;
	}
	
	public void delete() {
		this.isDeleted='Y';
	}
	
}