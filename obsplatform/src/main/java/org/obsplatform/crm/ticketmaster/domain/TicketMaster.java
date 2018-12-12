package org.obsplatform.crm.ticketmaster.domain;

import java.sql.Time;
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
import org.obsplatform.crm.ticketmaster.command.TicketMasterCommand;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "b_ticket_master")
public class TicketMaster {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "client_id", length = 65536)
	private Long clientId;

	@Column(name = "priority")
	private String priority;

	@Column(name = "problem_code")
	private Integer problemCode;

	@Column(name = "description")
	private String description;

	@Column(name = "ticket_date")
	private Date ticketDate;

	@Column(name = "status")
	private String status;

	@Column(name = "status_code")
	private Integer statusCode;

	@Column(name = "resolution_description")
	private String resolutionDescription;

	@Column(name = "assigned_to")
	private Long assignedTo;

	@Column(name = "source")
	private String source;

	@Column(name = "closed_date")
	private Date closedDate;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "createdby_id")
	private Long createdbyId;

	@Column(name = "source_of_ticket", length = 50)
	private String sourceOfTicket;

	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "lastmodifiedby_id")
	private Long lastModifyId;

	@Column(name = "lastmodified_date")
	private Date lastModifydate;

	@Column(name = "issue")
	private String issue;

	@Column(name = "dept_id")
	private Long departmentId;

	@Column(name = "appointment_date")
	private Date appointmentDate;

	@Column(name = "appointment_time")
	private Time appointmentTime;

	@Column(name = "followup_date")
	private Date nextCallDate;

	@Column(name = "followup_time")
	private Time nextCallTime;
	
	@Column(name="resolution_date")
	private Date resolutionDate;

	public TicketMaster() {

	}

	public static TicketMaster fromJson(final JsonCommand command) throws ParseException {

		final String priority = command.stringValueOfParameterNamed("priority");
		final Integer problemCode = command.integerValueOfParameterNamed("problemCode");
		final String description = command.stringValueOfParameterNamed("description");
		final Long assignedTo = command.longValueOfParameterNamed("assignedTo");
		// System.out.println("issue before***"+issue);
		final String issue = command.stringValueOfParameterNamed("issue");
		// System.out.println("issue after***"+issue);
		final LocalDate startDate = command.localDateValueOfParameterNamed("ticketDate");
		// System.out.println("startDate***"+startDate);
		final String startDateString = startDate.toString() + command.stringValueOfParameterNamed("ticketTime");
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final Date ticketDate = df.parse(startDateString);
		final Long departmentId = command.longValueOfParameterNamed("departmentId");

		final String statusCode = command.stringValueOfParameterNamed("problemDescription");
		final Long clientId = command.getClientId();
		final String sourceOfTicket = command.stringValueOfParameterNamed("sourceOfTicket");
		final String dueDate = command.stringValueOfParameterNamed("dueTime");
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dueTime;
		if (dueDate.equalsIgnoreCase("")) {
			dueTime = null;
		} else {
			dueTime = dateFormat.parse(dueDate);
		}
		final String status = command.stringValueOfParameterNamed("status");
		return new TicketMaster(clientId, priority, ticketDate, problemCode, description, statusCode, assignedTo, null,
				null, null, sourceOfTicket, dueTime, issue, status, departmentId);
	}

	public TicketMaster(final Integer statusCode, final Long assignedTo) {

		this.clientId = null;
		this.priority = null;
		this.ticketDate = null;
		this.problemCode = null;
		this.description = null;
		this.status = null;
		this.statusCode = statusCode;
		this.source = null;
		this.resolutionDescription = null;
		this.assignedTo = assignedTo;
		this.createdDate = null;
		this.createdbyId = null;
	}

	public TicketMaster(final Long clientId, final String priority, final Date ticketDate, final Integer problemCode,
			final String description, final String resolutionDescription, final Long assignedTo,
			final Integer statusCode, final Date createdDate, final Integer createdbyId, final String sourceOfTicket,
			final Date dueTime, final String issue, final String status, final Long departmentId) {

		this.clientId = clientId;
		this.priority = priority;
		this.ticketDate = ticketDate;
		this.problemCode = problemCode;
		this.description = description;
		this.status = status;
		this.statusCode = statusCode;
		this.source = "Manual";
		this.resolutionDescription = resolutionDescription;
		this.assignedTo = assignedTo;
		this.createdDate = DateUtils.getDateOfTenant();
		this.createdbyId = null;
		this.sourceOfTicket = sourceOfTicket;
		this.dueDate = dueTime;
		this.issue = issue;
		this.departmentId = departmentId;

	}
	
	

	public TicketMaster(Long id, Long clientId, String priority, Integer problemCode, String description,
			Date ticketDate, String status, Integer statusCode, String resolutionDescription, Long assignedTo,
			String source, Date closedDate, Date createdDate, Long createdbyId, String sourceOfTicket, Date dueDate,
			Long lastModifyId, Date lastModifydate, String issue, Long departmentId, Date appointmentDate,
			Time appointmentTime, Date nextCallDate, Time nextCallTime) {
		super();
		this.id = id;
		this.clientId = clientId;
		this.priority = priority;
		this.problemCode = problemCode;
		this.description = description;
		this.ticketDate = ticketDate;
		this.status = status;
		this.statusCode = statusCode;
		this.resolutionDescription = resolutionDescription;
		this.assignedTo = assignedTo;
		this.source = source;
		this.closedDate = closedDate;
		this.createdDate = createdDate;
		this.createdbyId = createdbyId;
		this.sourceOfTicket = sourceOfTicket;
		this.dueDate = dueDate;
		this.lastModifyId = lastModifyId;
		this.lastModifydate = lastModifydate;
		this.issue = issue;
		this.departmentId = departmentId;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.nextCallDate = nextCallDate;
		this.nextCallTime = nextCallTime;
	}


	public String getSource() {
		return source;
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getPriority() {
		return priority;
	}

	public Integer getProblemCode() {
		return problemCode;
	}

	public String getDescription() {
		return description;
	}

	public Date getTicketDate() {
		return ticketDate;
	}

	public String getStatus() {
		return status;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getResolutionDescription() {
		return resolutionDescription;
	}

	public Long getAssignedTo() {
		return assignedTo;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public String getSourceOfTicket() {
		return sourceOfTicket;
	}

	public void setSourceOfTicket(String sourceOfTicket) {
		this.sourceOfTicket = sourceOfTicket;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Long getLastModifyId() {
		return lastModifyId;
	}

	public void setLastModifyId(Long lastModifyId) {
		this.lastModifyId = lastModifyId;
	}

	public Date getLastModifydate() {
		return lastModifydate;
	}

	public void setLastModifydate(Date lastModifydate) {
		this.lastModifydate = lastModifydate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void setProblemCode(Integer problemCode) {
		this.problemCode = problemCode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTicketDate(Date ticketDate) {
		this.ticketDate = ticketDate;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public void setResolutionDescription(String resolutionDescription) {
		this.resolutionDescription = resolutionDescription;
	}

	public void setAssignedTo(Long assignedTo) {
		this.assignedTo = assignedTo;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void updateTicket(final TicketMasterCommand command) {
		this.status = command.getStatus();
		this.assignedTo = command.getAssignedTo();
		this.priority = command.getPriority();
		this.problemCode = command.getProblemCodeId();
		this.resolutionDescription = command.getResolutionDescription();
		if(command.getResolutionDate()!=null)
		 this.resolutionDate = command.getResolutionDate().toDate();
		this.departmentId = command.getDepartmentId();
		this.appointmentDate = command.getAppointmentDate();
		if (command.getAppointmentTime() != null) {
			Time appointmenttime = new Time(command.getAppointmentTime().toDateTimeToday().getMillis());
			this.appointmentTime = appointmenttime;
		} else
			this.appointmentTime = null;
		this.nextCallDate = command.getNextCallDate();
		if (command.getNextCallTime() != null) {
			Time followuptime = new Time(command.getNextCallTime().toDateTimeToday().getMillis());
			this.nextCallTime = followuptime;
		} else
			this.nextCallTime = null;
	}
	
	

	public void closeTicket(final JsonCommand command, final Long userId) {

		this.status = "CLOSED";
		this.statusCode = Integer.parseInt(command.stringValueOfParameterNamed("status"));
		this.resolutionDescription = command.stringValueOfParameterNamed("resolutionDescription");
		this.closedDate = DateUtils.getDateOfTenant();
		this.lastModifyId = userId;
		this.lastModifydate = DateUtils.getDateOfTenant();

	}

	public Date getClosedDate() {
		return closedDate;
	}

	/**
	 * @return the createdbyId
	 */
	public Long getCreatedbyId() {
		return createdbyId;
	}

	/**
	 * @param createdbyId
	 *            the createdbyId to set
	 */
	public void setCreatedbyId(final Long createdbyId) {
		this.createdbyId = createdbyId;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Time getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(Time appointmentTime) {
		this.appointmentTime = appointmentTime;
	}

	public Date getNextCallDate() {
		return nextCallDate;
	}

	public void setNextCallDate(Date nextCallDate) {
		this.nextCallDate = nextCallDate;
	}

	public Time getNextCallTime() {
		return nextCallTime;
	}

	public void setNextCallTime(Time nextCallTime) {
		this.nextCallTime = nextCallTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}