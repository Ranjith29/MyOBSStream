package org.obsplatform.crm.ticketmaster.command;

import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class TicketMasterCommand {

	private final Long id;
	private final Long clientId;
	private String priority;
	private final LocalDate ticketDate;
	private final String problemCode;
	private final String description;
	private final String status;
	private final String resolutionDescription;
	private final Long assignedTo;
	private final String comments;
	private final Long ticketId;
	private final Long createdbyId;
	private final Integer statusCode;
	private Integer problemCodeId;
	private String username;
	private String notes;
	private Long departmentId;
	private Date appointmentDate;
	private LocalTime appointmentTime;
	private Date nextCallDate;
	private LocalTime nextCallTime;
	private String issue;
	private LocalDate resolutionDate;
	
	
	public TicketMasterCommand(final Long clientId, final String priority,
								final String description, final String problemCode, final String status,
								final String resolutionDescription, final Long assignedTo, final LocalDate ticketDate,
								final Long createdbyId, final Integer statusCode,final String username,final String notes ){		
		
		this.id = null;
		this.clientId = clientId;
		this.priority = priority;
		this.ticketDate = ticketDate;
		this.problemCode = problemCode;
		this.description = description;
		this.status = status;
		this.resolutionDescription = resolutionDescription;
		this.assignedTo = assignedTo;
		this.comments = null;
		this.ticketId = null;
		this.createdbyId = createdbyId;
		this.statusCode = null;
		this.username =null;
		this.notes= notes;
	}
	public TicketMasterCommand(final Long ticketId, final String comments, final String status,
			final Long assignedTo, final Long createdbyId, final Integer statusCode, 
			final Integer problemCode, final String priority,final String resolutionDescription,final String username,final String notes,
			final Long departmentId, final Date appointmentDate,final LocalTime appointmentTime,final Date nextCallDate, 
			final LocalTime nextCallTime, final String issue, final LocalDate resolutionDate) {
		
		this.id = null;
		this.clientId = null;
		this.priority = null;
		this.ticketDate = null;
		this.problemCode = null;
		this.description = null;
		this.status = status;
		this.resolutionDescription = resolutionDescription;
		this.assignedTo = assignedTo;
		this.comments = comments;
		this.ticketId = ticketId;
		this.createdbyId = createdbyId;
		this.statusCode = statusCode;
		this.problemCodeId = problemCode;
		this.priority = priority;
		this.username =username;
		this.notes=notes;
		this.departmentId = departmentId;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.nextCallDate = nextCallDate;
		this.nextCallTime = nextCallTime;
		this.issue = issue;
		this.resolutionDate = resolutionDate;
	}
	public TicketMasterCommand(final String status, final String resolutionDescription, final Integer statusCode) {
		this.id = null;
		this.clientId = null;
		this.priority = null;
		this.ticketDate = null;
		this.problemCode = null;
		this.description = null;
		this.assignedTo = null;
		this.comments = null;
		this.ticketId = null;
		this.createdbyId = null;
		this.notes= null;
		this.statusCode = statusCode;
		this.status = "CLOSED";
		this.resolutionDescription = resolutionDescription;
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
	public LocalDate getTicketDate() {
		return ticketDate;
	}
	public String getProblemCode() {
		return problemCode;
	}
	public String getDescription() {
		return description;
	}
	public String getStatus() {
		return status;
	}
	public String getResolutionDescription() {
		return resolutionDescription;
	}
	public LocalDate getResolutionDate(){
		return resolutionDate;
	}
	public Long getAssignedTo() {
		return assignedTo;
	}
	public String getComments() {
		return comments;
	}
	public Long getTicketId() {
		return ticketId;
	}
	public Long getCreatedbyId() {
		return createdbyId;
	}
	/**
	 * @return the statusCode
	 */
	public Integer getStatusCode() {
		return statusCode;
	}
	public Integer getProblemCodeId() {
		return problemCodeId;
	}
	public void setProblemCodeId(Integer problemCodeId) {
		this.problemCodeId = problemCodeId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
	public LocalTime getAppointmentTime() {
		return appointmentTime;
	}
	public void setAppointmentTime(LocalTime appointmentTime) {
		this.appointmentTime = appointmentTime;
	}
	public Date getNextCallDate() {
		return nextCallDate;
	}
	public void setNextCallDate(Date nextCallDate) {
		this.nextCallDate = nextCallDate;
	}
	public LocalTime getNextCallTime() {
		return nextCallTime;
	}
	public void setNextCallTime(LocalTime nextCallTime) {
		this.nextCallTime = nextCallTime;
	}
	public void setIssue(String issue){
		this.issue = issue;
	}
	public String getIssue(){
		return issue;
	}
	
	
}
