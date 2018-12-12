package org.obsplatform.crm.ticketmaster.data;

import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.portfolio.client.data.ClientData;

public class TicketMasterData {
	
	private List<TicketMasterData> masterData;
	private Collection<MCodeData> statusType;
	private List<EnumOptionData> priorityType;
    private Collection<MCodeData> problemsDatas;
    private List<UsersData> usersData;
    private Collection<MCodeData> sourceData;
    private Long id;
    private String priority;
    private String status;
    private String assignedTo;
    private LocalDate ticketDate;
    private int userId;
    private String lastComment;
    private String problemDescription;
    private String userName;
    private Integer statusCode;
    private Integer problemCode;
    private String statusDescription;
	private LocalDate createdDate;
	private String attachedFile;
	private String sourceOfTicket;
	private Date dueDate;
	private String resolutionDescription;
	private String issue;
	private String ticketstatus;
	private LocalDate date;
	private ClientData clientData;
	private String assignFrom;
	private String Agentnotes;
	private Long departmentId;
	private LocalDate appointmentDate;
	private Time appointmentTime;
	private LocalDate nextCallDate;
	private Time nextCallTime;
	private String address;
	private String displayName;
	private Long clientId;

	
	public TicketMasterData(final List<EnumOptionData> statusType,
			final List<EnumOptionData> priorityType) {
		this.priorityType = priorityType;
		this.problemsDatas = null;
	}

	public TicketMasterData(final Collection<MCodeData> datas, final List<UsersData> userData,
			final List<EnumOptionData> priorityData, final Collection<MCodeData> sourceData) {
		
		this.problemsDatas = datas;
		this.usersData = userData;
		this.ticketDate = DateUtils.getLocalDateOfTenant();
		this.priorityType = priorityData;
		this.sourceData = sourceData;
	}

	public TicketMasterData(final Long id, final String priority, final String status, final Integer assignedTo, 
			final LocalDate ticketDate, final String lastComment, final String problemDescription, final String userName, 
			final String sourceOfTicket, final Date dueDate, final String description, final String issue,
			final Integer problemCode, final Integer statusCode, final String resolutionDescription, final Long departmentId,
			final LocalDate appointmentDate,final Time appointmentTime,final LocalDate nextCallDate,
			final Time nextCallTime,final String address,
			final String displayName, final Long clientId) {
		
		this.id = id;
		this.priority = priority;
		this.status = status;
		this.userId = assignedTo;
		this.ticketDate = ticketDate;
		this.lastComment = lastComment;
		this.problemDescription = problemDescription;
		this.userName = userName;
		this.sourceOfTicket = sourceOfTicket;
		this.dueDate = dueDate;
		this.statusDescription = description;
		this.setIssue(issue);
		this.problemCode = problemCode;
		this.statusCode = statusCode;
		this.resolutionDescription = resolutionDescription;
		this.departmentId = departmentId;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.nextCallDate = nextCallDate;
		this.nextCallTime = nextCallTime;
		this.address = address;
		this.displayName=displayName;
		this.clientId = clientId;
		
	}

	public TicketMasterData(final Integer statusCode, final String statusDesc) {
	     this.statusCode = statusCode;
	     this.statusDescription = statusDesc;
	 
	}

	public TicketMasterData(final Long id, final LocalDate createdDate,
			final String assignedTo, final String description, final String fileName, final String assignFrom,final String status, final String username, final String Agentnotes) {
		 this.id = id;
		 this.createdDate = createdDate;
		 this.assignedTo = assignedTo;
	     this.attachedFile = fileName;
	     this.statusDescription = description;
	     this.setAssignFrom(assignFrom);
	     this.status= status;
	     this.userName=username;
	     this.Agentnotes=Agentnotes;
	}

	public TicketMasterData(final String description, final List<TicketMasterData> data) {
		this.problemDescription = description;
		this.masterData = data;

	}

	public List<EnumOptionData> getPriorityType() {
		return priorityType;
	}

	public Collection<MCodeData> getProblemsDatas() {
		return problemsDatas;
	}

	public List<UsersData> getUsersDatas() {
		return usersData;
	}

	public List<UsersData> getUsersData() {
		return usersData;
	}

	public Long getId() {
		return id;
	}

	public String getPriority() {
		return priority;
	}

	public String getStatus() {
		return status;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public LocalDate getTicketDate() {
		return ticketDate;
	}

	public int getUserId() {
		return userId;
	}

	public String getLastComment() {
		return lastComment;
	}

	public String getProblemDescription() {
		return problemDescription;
	}

	public String getUserName() {
		return userName;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public void setStatusData(final Collection<MCodeData> statusdata) {
		
		this.statusType = statusdata;
	}

	public String getResolutionDescription() {
		return resolutionDescription;
	}

	public void setResolutionDescription(final String resolutionDescription) {
		this.resolutionDescription = resolutionDescription;
	}

	public void setUsersData(final List<UsersData> usersData) {
		this.usersData = usersData;
	}

	public void setPriorityType(List<EnumOptionData> priorityType) {
		this.priorityType = priorityType;
	}

	public void setProblemsDatas(Collection<MCodeData> problemsDatas) {
		this.problemsDatas = problemsDatas;
	}


	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void clientData(ClientData clientData) {
		
		this.setClientData(clientData);
	}

	/**
	 * @return the clientData
	 */
	public ClientData getClientData() {
		return clientData;
	}

	/**
	 * @param clientData the clientData to set
	 */
	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	/**
	 * @return the assignFrom
	 */
	public String getAssignFrom() {
		return assignFrom;
	}

	/**
	 * @param assignFrom the assignFrom to set
	 */
	public void setAssignFrom(String assignFrom) {
		this.assignFrom = assignFrom;
	}

	/**
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}

	/**
	 * @param issue the issue to set
	 */
	public void setIssue(String issue) {
		this.issue = issue;

	}
	
	public String getAgentnotes() {
		return Agentnotes;
	}

	public void setAgentnotes(String agentnotes) {
		Agentnotes = agentnotes;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Time getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(Time appointmentTime) {
		this.appointmentTime = appointmentTime;
	}

	public LocalDate getNextCallDate() {
		return nextCallDate;
	}

	public void setNextCallDate(LocalDate nextCallDate) {
		this.nextCallDate = nextCallDate;
	}

	public Time getNextCallTime() {
		return nextCallTime;
	}

	public void setNextCallTime(Time nextCallTime) {
		this.nextCallTime = nextCallTime;
	}
	
	
	
}