package org.obsplatform.crm.ticketmaster.service;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.crm.ticketmaster.data.ClientTicketData;
import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.crm.ticketmaster.data.UsersData;
import org.obsplatform.crm.ticketmaster.domain.PriorityType;
import org.obsplatform.crm.ticketmaster.domain.PriorityTypeEnum;
import org.obsplatform.crm.ticketmaster.domain.TicketDetail;
import org.obsplatform.crm.ticketmaster.domain.TicketHistory;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TicketMasterReadPlatformServiceImpl implements TicketMasterReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ClientTicketData> paginationHelper = new PaginationHelper<ClientTicketData>();

	@Autowired
	public TicketMasterReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
		
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<UsersData> retrieveUsers() {
		
		context.authenticatedUser();
		final UserMapper mapper = new UserMapper();
		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class UserMapper implements RowMapper<UsersData> {

		public String schema() {
			return "u.id as id,u.username as username from m_appuser u where u.is_deleted=0";
		}

		@Override
		public UsersData mapRow(ResultSet resultSet, int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String username = resultSet.getString("username");
			final UsersData data = new UsersData(id, username);

			return data;
		}
	}

	@Override
	public Page<ClientTicketData> retrieveAssignedTicketsForNewClient(SearchSqlQuery searchTicketMaster, String statusType, String assignTo) {
		
		final AppUser user = this.context.authenticatedUser();
		final String hierarchy = user.getOffice().getHierarchy();
		final String hierarchySearchString = hierarchy + "%";
		final UserTicketsMapperForNewClient mapper = new UserTicketsMapperForNewClient();

		StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(mapper.userTicketSchema());
        sqlBuilder.append(" where tckt.id IS NOT NULL and o.hierarchy like ? ");
        
        String sqlSearch = searchTicketMaster.getSqlSearch();
        String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch = sqlSearch.trim();
	    	extraCriteria = " and ((select display_name from m_client where id = tckt.client_id) like '%"+sqlSearch+"%' OR" 
	    			+ " (select mcv.code_value from m_code_value mcv where mcv.id = tckt.problem_code) like '%"+sqlSearch+"%' OR"
	    			+ " (select CONCAT(cad.address_no,',',cad.street) as address from b_client_address cad  where cad.client_id = tckt.client_id AND cad.address_key = 'PRIMARY') like '%"+sqlSearch+"%' OR"
	    			+ " tckt.status like '%"+sqlSearch+"%' OR"
	    			+ " (select user.username from m_appuser user where tckt.assigned_to = user.id) like '%"+sqlSearch+"%')";
	    }
	    
	    if(statusType != null){
	    	extraCriteria += " and tckt.status='"+statusType+"'";
	    }
	    sqlBuilder.append(extraCriteria);
	    if(assignTo != null){
	    	sqlBuilder.append(" and tckt.assigned_to = (select user.id from m_appuser user where user.username='"+assignTo+"')");
	    }
	    if(statusType != null){
	    	if(statusType.equalsIgnoreCase("APPOINTMENT")){
		    	sqlBuilder.append(" order by tckt.appointment_date,appointment_time  ");
		    }
		    else if(statusType.equalsIgnoreCase("FollowUp")){
		    	sqlBuilder.append(" order by tckt.followup_date,followup_time  ");
		    }
		    else{
		    	sqlBuilder.append(" group by id order by tckt.id desc ");
		    	}
	    }
	    else{
	    	sqlBuilder.append(" group by id order by tckt.id desc ");
	    }
	    
        if (searchTicketMaster.isLimited()) {
            sqlBuilder.append(" limit ").append(searchTicketMaster.getLimit());
        }

        if (searchTicketMaster.isOffset()) {
            sqlBuilder.append(" offset ").append(searchTicketMaster.getOffset());
        }

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()", sqlBuilder.toString(),
	            new Object[] {hierarchySearchString}, mapper);

	}

	@Override
	public List<TicketMasterData> retrieveClientTicketDetails(final Long clientId) {
		try {
			final ClientTicketMapper mapper = new ClientTicketMapper();

			final String sql = "select " + mapper.clientOrderLookupSchema()	+ " and tckt.client_id= ? order by tckt.id DESC ";

			return jdbcTemplate.query(sql, mapper, new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientTicketMapper implements RowMapper<TicketMasterData> {

		public String clientOrderLookupSchema() {

			return "tckt.id as id, tckt.priority as priority, tckt.ticket_date as ticketDate, tckt.assigned_to as userId,tckt.source_of_ticket as sourceOfTicket, "
					+ " tckt.problem_code as problemCode, tckt.status_code as statusCode, tckt.due_date as dueDate, tckt.resolution_description as resolutionDescription,tckt.dept_id as departmentId,"
					+ "(SELECT code_value FROM m_code_value mcv WHERE  tckt.status_code = mcv.id) AS ticketstatus,"
					+ " tckt.issue as issue,tckt.description as description, "
					+ " (select code_value from m_code_value mcv where tckt.problem_code=mcv.id)as problemDescription,"
					+ " tckt.status as status, "
					+ " (select m_appuser.username from m_appuser "
					+ " inner join b_ticket_details td on td.assigned_to = m_appuser.id"
					+ " where td.id = (select max(id) from b_ticket_details where b_ticket_details.ticket_id = tckt.id)) as assignedTo,"
					+ " (select comments FROM b_ticket_details details where details.ticket_id =tckt.id and "
					+ " details.id=(select max(id) from b_ticket_details where b_ticket_details.ticket_id = tckt.id)) as lastComment,"
					+ " tckt.appointment_date as appointmentdate,tckt.appointment_time as appointmenttime,"
					+ " tckt.followup_date as nextcalldate,tckt.followup_time as nextcalltime,"
					+ " tckt.client_id as clientId "
					+ " from b_ticket_master tckt, m_appuser user where tckt.assigned_to = user.id";
		}

		@Override
		public TicketMasterData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String priority = resultSet.getString("priority");
			final String status = resultSet.getString("status");
			final String LastComment = resultSet.getString("LastComment");
			final String problemDescription = resultSet.getString("problemDescription");
			final String assignedTo = resultSet.getString("assignedTo");
			final String usersId = resultSet.getString("userId");
			final LocalDate ticketDate = JdbcSupport.getLocalDate(resultSet, "ticketDate");
			final int userId = new Integer(usersId);
			final String sourceOfTicket = resultSet.getString("sourceOfTicket");
			final Date dueDate = resultSet.getTimestamp("dueDate");
			final String issue = resultSet.getString("issue");
			final String description = resultSet.getString("description");
			final Integer problemCode = resultSet.getInt("problemCode");
			final Integer statusCode = resultSet.getInt("statusCode");
			final String resolutionDescription = resultSet.getString("resolutionDescription");
			final Long departmentId = resultSet.getLong("departmentId");
			final LocalDate appointmentDate = JdbcSupport.getLocalDate(resultSet, "appointmentdate");
			final Time appointmentTime = resultSet.getTime("appointmenttime");
			final LocalDate nextcalldate = JdbcSupport.getLocalDate(resultSet, "nextcalldate");
			final Time nextcalltime = resultSet.getTime("nextcalltime");
			final Long clientId = resultSet.getLong("clientId");

			return new TicketMasterData(id, priority, status, userId, ticketDate, LastComment, problemDescription, assignedTo,
					sourceOfTicket, dueDate, description, issue, problemCode, statusCode, resolutionDescription, departmentId,
					appointmentDate, appointmentTime, nextcalldate, nextcalltime, null, null, clientId);
		}
	}

	@Override
	public TicketMasterData retrieveSingleTicketDetails(final Long clientId, final Long ticketId) {
		try {
			final ClientTicketMapper mapper = new ClientTicketMapper();
			final String sql = "select " + mapper.clientOrderLookupSchema() + " and tckt.client_id= ? and tckt.id=?";
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientId, ticketId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<EnumOptionData> retrievePriorityData() {
		EnumOptionData low = PriorityTypeEnum.priorityType(PriorityType.LOW);
		EnumOptionData medium = PriorityTypeEnum.priorityType(PriorityType.MEDIUM);
		EnumOptionData high = PriorityTypeEnum.priorityType(PriorityType.HIGH);
		List<EnumOptionData> priorityType = Arrays.asList(low, medium, high);
		return priorityType;
	}

	@Override
	public List<TicketMasterData> retrieveClientTicketHistory(final Long ticketId, final String historyParam) {
		
		context.authenticatedUser();

		if ("comment".equalsIgnoreCase(historyParam)) {

			final TicketDataMapper mapper = new TicketDataMapper();
			String sql = "select " + mapper.schema() + " where t.ticket_id=tm.id and t.ticket_id=? and t.comments is not null order by t.id DESC";
			return this.jdbcTemplate.query(sql, mapper, new Object[] { ticketId });
		} else if ("notes".equalsIgnoreCase(historyParam)) {
			final TicketDataMapper mapper = new TicketDataMapper();
			String sql = "select " + mapper.schema() + " where t.ticket_id=tm.id and t.ticket_id=? and t.notes not in('','undefined') order by t.id DESC";
			return this.jdbcTemplate.query(sql, mapper, new Object[] { ticketId });
		} else {
			final TickethistoryMapper mapper = new TickethistoryMapper();
			String sql = "select " + mapper.historyschema() + " where th.ticket_id = tm.id and th.ticket_id=?  and td.ticket_id = ? group by th.id order by th.id DESC";
			return this.jdbcTemplate.query(sql, mapper, new Object[] {ticketId, ticketId });
		}
	}

	private static final class TickethistoryMapper implements RowMapper<TicketMasterData> {

		public String historyschema() {
			return " th.id AS id, th.created_date AS createDate, user.firstname AS assignedTo, th.assign_from as assignFrom, th.status as status,"
					+ " td.attachments as attachements from ( b_ticket_master tm, b_ticket_history th, b_ticket_details td) inner join m_appuser user ON "
					+ "user.id = th.assigned_to";

		}

		@Override
		public TicketMasterData mapRow(ResultSet resultSet, int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final LocalDate createdDate = JdbcSupport.getLocalDate(resultSet, "createDate");
			final String assignedTo = resultSet.getString("assignedTo");
			final String assignFrom = resultSet.getString("assignFrom");
			final String status = resultSet.getString("status");
			String fileName = null;
			final TicketMasterData data = new TicketMasterData(id, createdDate, assignedTo, null, fileName, assignFrom, status, null, null);
			return data;
		}
	}

	private static final class TicketDataMapper implements RowMapper<TicketMasterData> {

		public String schema() {
			return " t.id AS id,t.created_date AS createDate,user.username AS assignedTo,t.comments as description, t.assign_from as assignFrom, "
					+ " t.attachments AS attachments, t.status as status, t.username as username, t.notes as Agentnotes  FROM b_ticket_master tm , b_ticket_details t  "
					+ " inner join m_appuser user on user.id = t.assigned_to ";
		}

		@Override
		public TicketMasterData mapRow(ResultSet resultSet, int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final LocalDate createdDate = JdbcSupport.getLocalDate(resultSet, "createDate");
			final String assignedTo = resultSet.getString("assignedTo");
			final String description = resultSet.getString("description");
			final String attachments = resultSet.getString("attachments");
			final String assignFrom = resultSet.getString("assignFrom");
			final String status = resultSet.getString("status");
			final String username = resultSet.getString("username");
			final String Agentnotes = resultSet.getString("Agentnotes");
			String fileName = null;
			if (attachments != null) {
				File file = new File(attachments);
				fileName = file.getName();
			}
			final TicketMasterData data = new TicketMasterData(id, createdDate, assignedTo, description, fileName, assignFrom, 
					status, username, Agentnotes);

			return data;
		}

	}

	private static final class UserTicketsMapperForNewClient implements RowMapper<ClientTicketData> {

		public String userTicketSchema() {

			return " SQL_CALC_FOUND_ROWS DISTINCT tckt.id AS id,tckt.client_id AS clientId,mct.display_name as clientName, mct.phone as teleNumber," 
			        + " tckt.priority AS priority,"
					+ "tckt.status AS status,tckt.ticket_date AS ticketDate,tckt.description as shortdescription,"
					+ "(SELECT user.username FROM m_appuser user WHERE tckt.createdby_id = user.id) AS created_user,"
					+ "tckt.assigned_to AS userId,"
					+ "(SELECT mcv.code_value FROM m_code_value mcv WHERE mcv.id = tckt.problem_code) AS problemDescription,"
					+ "(SELECT user.firstname FROM m_appuser user WHERE tckt.assigned_to = user.id) AS assignedTo,"
					+ "CONCAT(TIMESTAMPDIFF(day, tckt.ticket_date, '"
					+ DateUtils.getDateTimeOfTenant()
					+ "'), ' d ', MOD(TIMESTAMPDIFF(hour, tckt.ticket_date, '"
					+ DateUtils.getDateTimeOfTenant()
					+ "'), 24), ' hr ',"
					+ "MOD(TIMESTAMPDIFF(minute, tckt.ticket_date, '"
					+ DateUtils.getDateTimeOfTenant()
					+ "'), 60), ' min ') AS timeElapsed,"
					+ "IFNull((SELECT user.username FROM m_appuser user WHERE tckt.lastmodifiedby_id = user.id),'Null') AS closedby_user,"
					+ "CONCAT(boa.address_no,',',boa.street) as address,"
					+ "tckt.appointment_date as installationdate,tckt.appointment_time as installationtime, "
					+ "tckt.followup_date as followupDate,tckt.followup_time as followupTime, tckt.resolution_date as resolutionDate "
					+ "FROM b_ticket_master tckt left join m_client mct on mct.id = tckt.client_id "
					+ "left join m_office o on o.id = mct.office_id "
					+ "left join b_client_address boa on boa.client_id=mct.id ";
		}

		@Override
		public ClientTicketData mapRow(ResultSet resultSet, int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String priority = resultSet.getString("priority");
			final String status = resultSet.getString("status");
			final Long userId = resultSet.getLong("userId");
			final LocalDate ticketDate = JdbcSupport.getLocalDate(resultSet, "ticketDate");
			final String shortdescription = resultSet.getString("shortdescription");
			final String problemDescription = resultSet.getString("problemDescription");
			final String assignedTo = resultSet.getString("assignedTo");
			final Long clientId = resultSet.getLong("clientId");
			final String timeElapsed = resultSet.getString("timeElapsed");
			final String clientName = resultSet.getString("clientName");
			final String createUser = resultSet.getString("created_user");
			final String closedByuser = resultSet.getString("closedby_user");
			final String address = resultSet.getString("address");
			final LocalDate installationDate = JdbcSupport.getLocalDate(resultSet, "installationdate");
			final Time installationTime = resultSet.getTime("installationtime");
			final LocalDate followupDate = JdbcSupport.getLocalDate(resultSet, "followupDate");
			final Time followupTime = resultSet.getTime("followupTime");
			final LocalDate resolutionDate = JdbcSupport.getLocalDate(resultSet, "resolutionDate");
			final Long teleNumber = resultSet.getLong("teleNumber");

			return new ClientTicketData(id, priority, status, userId, ticketDate, shortdescription, problemDescription,
					assignedTo, clientId, timeElapsed, clientName, createUser, closedByuser, address, installationDate, 
					installationTime, followupDate, followupTime, resolutionDate, teleNumber);
		}
	}

	@Override
	public TicketMasterData retrieveTicket(final Long clientId, final Long ticketId) {

		try {
			final ClientTicketMapper mapper = new ClientTicketMapper();
			final String sql = "select " + mapper.clientOrderLookupSchema() + " and tckt.client_id= ? and tckt.id=?";

			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientId, ticketId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public TicketDetail retrieveTicketDetail(final Long ticketId) {

		try {
			final TicketDetailMapper mapper = new TicketDetailMapper();
			final String sql = "select "+ mapper.ticketdetailSchema() + " where td.id=(select max(t.id) as id from b_ticket_details t where t.ticket_id=?)";
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { ticketId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class TicketDetailMapper implements RowMapper<TicketDetail> {

		public String ticketdetailSchema() {

			return "max(td.id) as id, td.Assign_from as assignFrom, td.assigned_to as assignedTo from b_ticket_details td";
		}

		@Override
		public TicketDetail mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String assignfrom = resultSet.getString("assignFrom");
			final Long assignedTo = resultSet.getLong("assignedTo");

			return new TicketDetail(id, assignedTo, assignfrom);
		}
	}

	@Override
	public TicketHistory retrieveTickethistory(final Long ticketId) {

		try {
			final TicketHistoryMapperRow mapper = new TicketHistoryMapperRow();
			final String sql = "select " + mapper.tickethistorySchema() + " where th.id=(select max(t.id) as id from b_ticket_history t where t.ticket_id=?)";
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { ticketId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class TicketHistoryMapperRow implements RowMapper<TicketHistory> {

		public String tickethistorySchema() {

			return "max(th.id) as id, th.status as status, th.assigned_to as assignedTo from b_ticket_history th";
		}

		@Override
		public TicketHistory mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String status = resultSet.getString("status");
			final Long assignedTo = resultSet.getLong("assignedTo");

			return new TicketHistory(id, assignedTo, status);
		}
	}

	@Override
	public Object retrieveIssueIdOfLastTicket() {
		try {
			final String sql = "select max(id) from b_ticket_master";
			int issueId = jdbcTemplate.queryForInt(sql);
			Object result = (Integer) issueId;
			return result;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<TicketMasterData> retrieveTicketsByDate(String statusType, String date) {

		final TicketByDateMapper mapper = new TicketByDateMapper();
		String sql = "select " + mapper.TicketByDateLookupSchema()
				+ "and (c.id in (tckt.client_id))" + " and (tckt.status='" + statusType + "') and ";
		if (statusType.equalsIgnoreCase("Appointment")) {
			sql = sql + "(tckt.appointment_date='" + date + "')  ";
			sql = sql + "order by tckt.appointment_time  ";
		}
		else {
			sql = sql + "(tckt.followup_date='" + date + "') ";
			sql = sql + "order by tckt.followup_time  ";
		}

		return jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class TicketByDateMapper implements RowMapper<TicketMasterData> {

		public String TicketByDateLookupSchema() {

			return "tckt.id as id, tckt.priority as priority, tckt.ticket_date as ticketDate, tckt.assigned_to as userId,tckt.source_of_ticket as sourceOfTicket, "
					+ " tckt.problem_code as problemCode, tckt.status_code as statusCode, tckt.due_date as dueDate, tckt.resolution_description as resolutionDescription,tckt.dept_id as departmentId,"
					+ "(SELECT code_value FROM m_code_value mcv WHERE  tckt.status_code = mcv.id) AS ticketstatus,"
					+ " tckt.issue as issue,tckt.description as description, "
					+ " (select code_value from m_code_value mcv where tckt.problem_code=mcv.id)as problemDescription,"
					+ " tckt.status as status, "
					+ " (select m_appuser.username from m_appuser "
					+ " inner join b_ticket_details td on td.assigned_to = m_appuser.id"
					+ " where td.id = (select max(id) from b_ticket_details where b_ticket_details.ticket_id = tckt.id)) as assignedTo,"
					+ " (select comments FROM b_ticket_details details where details.ticket_id =tckt.id and "
					+ " details.id=(select max(id) from b_ticket_details where b_ticket_details.ticket_id = tckt.id)) as lastComment,"
					+ " tckt.appointment_date as installationdate, tckt.appointment_time as appointmenttime,tckt.followup_date as followupDate,  "
					+ " c.display_name as displayName,CONCAT(ca.address_no,',',ca.street) as address, "
					+ " tckt.client_id as clientId "
					+ " from b_ticket_master tckt, m_appuser user, "
					+ " m_client c join b_client_address ca on c.id = ca.client_id "
					+ " where (tckt.assigned_to = user.id) ";
		}

		@Override
		public TicketMasterData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String priority = resultSet.getString("priority");
			final String status = resultSet.getString("status");
			final String LastComment = resultSet.getString("LastComment");
			final String problemDescription = resultSet.getString("problemDescription");
			final String assignedTo = resultSet.getString("assignedTo");
			final String usersId = resultSet.getString("userId");
			final LocalDate ticketDate = JdbcSupport.getLocalDate(resultSet, "ticketDate");
			final int userId = new Integer(usersId);
			final String sourceOfTicket = resultSet.getString("sourceOfTicket");
			final Date dueDate = resultSet.getTimestamp("dueDate");
			final String issue = resultSet.getString("issue");
			final String description = resultSet.getString("description");
			final Integer problemCode = resultSet.getInt("problemCode");
			final Integer statusCode = resultSet.getInt("statusCode");
			final String resolutionDescription = resultSet.getString("resolutionDescription");
			final Long departmentId = resultSet.getLong("departmentId");
			final Time appointmentTime = resultSet.getTime("appointmenttime");
			final LocalDate installationdate = JdbcSupport.getLocalDate(resultSet, "installationdate");
			final String displayName = resultSet.getString("displayName");
			final String address = resultSet.getString("address");
			final LocalDate followupDate = JdbcSupport.getLocalDate(resultSet, "followupDate");
			final Long clientId = resultSet.getLong("clientId");

			return new TicketMasterData(id, priority, status, userId, ticketDate, LastComment, problemDescription, assignedTo,
					sourceOfTicket, dueDate, description, issue, problemCode, statusCode, resolutionDescription, departmentId,
					installationdate, appointmentTime, followupDate, null, address, displayName, clientId);
		}
	}
}
