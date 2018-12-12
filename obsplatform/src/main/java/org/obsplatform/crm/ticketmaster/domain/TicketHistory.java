package org.obsplatform.crm.ticketmaster.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "b_ticket_history")
public class TicketHistory {
	
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "ticket_id", length = 65536)
	private Long ticketId;

	@Column(name = "status")
	private String status;
	
	@Column(name = "created_date")
	private Date createdDate;

	@Column(name="Assign_from")
	private String assignFrom;
	
	@Column(name = "assigned_to")
	private Long assignedTo;
	
	@Column(name = "createdby_id")
	private Long createdbyId;
	
	public static TicketHistory fromJson(JsonCommand command) throws ParseException {
		
		final Long assignedTo = command.longValueOfParameterNamed("assignedTo");
		final String status  = command.stringValueOfParameterNamed("status");
		final LocalDate startDate = command.localDateValueOfParameterNamed("ticketDate");
		final String startDateString = startDate.toString() + command.stringValueOfParameterNamed("ticketTime");
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final Date ticketDate = df.parse(startDateString);
		final String assignFrom = command.stringValueOfParameterNamed("assignFrom");
		return new TicketHistory(assignedTo, status, ticketDate, assignFrom);
	}
	
	public TicketHistory(Long assignedTo, String status, Date ticketDate, String assignFrom) {
		this.assignedTo = assignedTo;
		this.status = status;
		this.createdDate = ticketDate;
		this.assignFrom = assignFrom;
		
		
	}
	

	public TicketHistory(Long ticketId, Long assignedTo, String status,String assignFrom) {
		
		this.ticketId = ticketId;
		this.assignedTo = assignedTo;
		this.status = status;
		this.createdDate = DateUtils.getLocalDateOfTenant().toDate();
		this.assignFrom = assignFrom;
		
	}
public TicketHistory(Long id, Long assignedTo, String status) {
		
		this.id=id;
		this.assignedTo = assignedTo;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getAssignFrom() {
		return assignFrom;
	}

	public void setAssignFrom(String assignFrom) {
		this.assignFrom = assignFrom;
	}

	public Long getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(Long assignedTo) {
		this.assignedTo = assignedTo;
	}
	
	public void setCreatedbyId(final Long createdbyId) {
		this.createdbyId = createdbyId;
	}
	
	public Long getCreatedbyId() {
		return createdbyId;
	}
		

}
