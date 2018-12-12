package org.obsplatform.crm.clientprospect.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obsplatform.crm.clientprospect.data.ClientProspectCardDetailsData;
import org.obsplatform.crm.clientprospect.data.ClientProspectData;
import org.obsplatform.crm.clientprospect.data.ProspectDetailAssignedToData;
import org.obsplatform.crm.clientprospect.data.ProspectDetailData;
import org.obsplatform.crm.clientprospect.data.ProspectPlanCodeData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientProspectReadPlatformServiceImp implements
		ClientProspectReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ClientProspectData> paginationHelper = new PaginationHelper<ClientProspectData>();
	private final String SQLSEARCHPARAMETER = "%' OR";
	private String extraCriteria = "";

	@Autowired
	public ClientProspectReadPlatformServiceImp(
			final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}

	public Collection<ClientProspectData> retriveClientProspect() {
		
		context.authenticatedUser();
		final ClientProspectMapper rowMapper = new ClientProspectMapper();
		final String sql = "select " + rowMapper.query();
		return jdbcTemplate.query(sql, rowMapper);
	}

	public Page<ClientProspectData> retriveClientProspect(final SearchSqlQuery searchClientProspect, final Long userId) {

		final ClientProspectMapperForNewClient rowMapper = new ClientProspectMapperForNewClient();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ").append(rowMapper.query())
				.append(" where p.is_deleted = 'N' | 'Y' ")
				.append(" and p.createdby_id = " + userId);

		String sqlSearch = searchClientProspect.getSqlSearch();
			
		if (sqlSearch != null) {
			sqlSearch = sqlSearch.trim();
			extraCriteria = " and (p.mobile_number like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.email like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.status like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.address like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " concat(ifnull(p.first_name, ''), if(p.first_name > '',' ', '') , ifnull(p.last_name, '')) like '%"
					+ sqlSearch + "%') ";
		}
		sqlBuilder.append(extraCriteria);

		if (searchClientProspect.isLimited()) {
			sqlBuilder.append(" limit ")
					.append(searchClientProspect.getLimit());
		}

		if (searchClientProspect.isOffset()) {
			sqlBuilder.append(" offset ").append(
					searchClientProspect.getOffset());
		}

		final String sqlCountRows = "SELECT FOUND_ROWS()";
		return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows,
				sqlBuilder.toString(), new Object[] {}, rowMapper);

	}

	@Override
	public ProspectDetailData retriveClientProspect(Long clientProspectId) {
		return new ProspectDetailData();
	}

	@Override
	public List<ProspectDetailAssignedToData> retrieveUsers() {
		context.authenticatedUser();

		UserMapper mapper = new UserMapper();

		String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	@Override
	public List<ProspectDetailData> retriveProspectDetailHistory(
			final Long prospectdetailid, final Long userId) {
		
		context.authenticatedUser();
		HistoryMapper mapper = new HistoryMapper();
		String sql = "select " + mapper.query() + " where p.id = d.prospect_id and p.createdby_id=? and d.prospect_id=? order by d.id desc";
		return jdbcTemplate.query(sql, mapper, new Object[] { userId, prospectdetailid });
	}

	private static final class HistoryMapper implements RowMapper<ProspectDetailData> {
		@Override
		public ProspectDetailData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			Long prospectId = rs.getLong("prospectId");
			String callStatus = rs.getString("callStatus");
			Date nextTime = rs.getTimestamp("nextTime");
			String notes = rs.getString("notes");
			String assignedTo = rs.getString("assignedTo");
			return new ProspectDetailData(id, prospectId, callStatus,
					DateFormat.getDateTimeInstance().format(nextTime), notes, assignedTo);
		}

		public String query() {
			return "d.id as id, d.prospect_id as prospectId, d.next_time as nextTime, d.notes as notes, cv.code_value as callStatus, au.username as assignedTo from b_prospect p, b_prospect_detail d left outer join m_code_value cv on d.call_status=cv.id left outer join m_appuser au on au.id=d.assigned_to";
		}
	}

	private static final class UserMapper implements
			RowMapper<ProspectDetailAssignedToData> {

		public String schema() {
			return "u.id as id,u.username as assignedTo from m_appuser u where u.is_deleted=0";

		}

		@Override
		public ProspectDetailAssignedToData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("assignedTo");
			return new ProspectDetailAssignedToData(id, username);

		}

	}

	public class ClientProspectMapper implements RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			Short prospectType = rs.getShort("prospectType");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String homePhoneNumber = rs.getString("homePhoneNumber");
			String workPhoneNumber = rs.getString("workPhoneNumber");
			String mobileNumber = rs.getString("mobileNumber");
			String email = rs.getString("email");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			String preferredPlan = rs.getString("preferredPlan");
			Date preferredCallingTime = rs.getDate("preferredCallingTime");
			String note = rs.getString("note");
			String address = rs.getString("address");
			String streetArea = rs.getString("streetArea");
			String cityDistrict = rs.getString("cityDistrict");
			String state = rs.getString("state");
			String country = rs.getString("country");
			String status = rs.getString("status");
			String statusRemark = rs.getString("statusRemark");
			String isDeleted = rs.getString("isDeleted");
			String actionName = rs.getString("actionName");
			
			return new ClientProspectData(id, prospectType, firstName,
					middleName, lastName, homePhoneNumber, workPhoneNumber,
					mobileNumber, email, sourceOfPublicity,
					preferredCallingTime, note, address, streetArea,
					cityDistrict, state, country, preferredPlan, status,
					statusRemark, isDeleted, actionName);
		}

		public String query() {
			// String sql =
			// "p.id as id, p.prospect_type as prospectType, p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, p.home_phone_number as homePhoneNumber, p.work_phone_number as workPhoneNumber, p.mobile_number as mobileNumber, p.email as email, p.source_of_publicity as sourceOfPublicity, p.preferred_plan as preferredPlan, p.preferred_calling_time as preferredCallingTime, p.address as address, p.street_area as streetArea, p.city_district as cityDistrict, p.state as state, p.country as country, p.status as status, p.status_remark as statusRemark, p.is_deleted as isDeleted, (select notes FROM b_prospect_detail pd where pd.prospect_id =p.id and pd.id=(select max(id) from b_prospect_detail where b_prospect_detail.prospect_id = p.id)) as note";
			String sql = "SQL_CALC_FOUND_ROWS p.id as id, p.prospect_type as prospectType, "
					+ "p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, "
					+ "p.home_phone_number as homePhoneNumber, p.work_phone_number as workPhoneNumber, "
					+ "p.mobile_number as mobileNumber, p.email as email, p.source_of_publicity as sourceOfPublicity, "
					+ "p.preferred_plan as preferredPlan, p.preferred_calling_time as preferredCallingTime, "
					+ "p.address as address, p.street_area as streetArea, p.city_district as cityDistrict, "
					+ "p.state as state, p.country as country, p.status as status, p.status_remark as statusRemark, "
					+ "p.is_deleted as isDeleted, "
					+ "(select notes FROM b_prospect_detail pd where pd.prospect_id =p.id and  "
					+ "pd.id=(select max(id) from b_prospect_detail where b_prospect_detail.prospect_id = p.id)) as note, "
					+ "(select is_deleted from b_eventaction_mapping where action_name='Prospect Quotation') AS actionName from b_prospect p ";

			return sql;
		}
	}

	public class ClientProspectMapperForNewClient implements
			RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			Short prospectType = rs.getShort("prospectType");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String homePhoneNumber = rs.getString("homePhoneNumber");
			String workPhoneNumber = rs.getString("workPhoneNumber");
			String mobileNumber = rs.getString("mobileNumber");
			String email = rs.getString("email");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			String preferredPlan = rs.getString("preferredPlan");
			Date preferredCallingTime = rs.getDate("preferredCallingTime");
			String note = rs.getString("note");
			String address = rs.getString("address");
			String streetArea = rs.getString("streetArea");
			String cityDistrict = rs.getString("cityDistrict");
			String state = rs.getString("state");
			String country = rs.getString("country");
			String status = rs.getString("status");
			String statusRemark = rs.getString("statusRemark");
			String isDeleted = rs.getString("isDeleted");
			String actionName = rs.getString("actionName");
			
			return new ClientProspectData(id, prospectType, firstName,
					middleName, lastName, homePhoneNumber, workPhoneNumber,
					mobileNumber, email, sourceOfPublicity,
					preferredCallingTime, note, address, streetArea,
					cityDistrict, state, country, preferredPlan, status,
					statusRemark, isDeleted, actionName);
		}

		public String query() {
			
			String sql = "SQL_CALC_FOUND_ROWS p.id as id, p.prospect_type as prospectType, "
					+ "p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, "
					+ "p.home_phone_number as homePhoneNumber, p.work_phone_number as workPhoneNumber, "
					+ "p.mobile_number as mobileNumber, p.email as email, p.source_of_publicity as sourceOfPublicity, "
					+ "p.preferred_plan as preferredPlan, p.preferred_calling_time as preferredCallingTime, "
					+ "p.address as address, p.street_area as streetArea, p.city_district as cityDistrict, "
					+ "p.state as state, p.country as country, p.status as status, p.status_remark as statusRemark, "
					+ "p.is_deleted as isDeleted, "
					+ "(select notes FROM b_prospect_detail pd where pd.prospect_id =p.id and pd.id=(select max(id) from b_prospect_detail where b_prospect_detail.prospect_id = p.id)) as note, "
					+ "(select is_deleted from b_eventaction_mapping where action_name='Prospect Quotation') AS actionName from b_prospect p ";
			return sql;
		}
	}

	@Override
	public Collection<ProspectPlanCodeData> retrivePlans() {
		context.authenticatedUser();

		final String sql = "select s.id as id,s.plan_description as planDescription,s.is_prepaid as isPrepaid from b_plan_master s where s.plan_status=1 and  s.is_deleted='n'  ";

		final RowMapper<ProspectPlanCodeData> rm = new PeriodMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	private static final class PeriodMapper implements
			RowMapper<ProspectPlanCodeData> {

		@Override
		public ProspectPlanCodeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String planCode = rs.getString("planDescription");
			String isPrepaid = rs.getString("isPrepaid");
			return new ProspectPlanCodeData(id, planCode, isPrepaid);

		}

	}

	@Override
	public ClientProspectData retriveSingleClient(Long id, Long userId) {

		context.authenticatedUser();
		final EditClientProspectMapper rowMapper = new EditClientProspectMapper();
		final String sql = "select " + rowMapper.query()
				+ " from b_prospect p where id=? and p.createdby_id=?";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { id, userId });
	}

	public class EditClientProspectMapper implements RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			Short prospectType = rs.getShort("prospectType");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String homePhoneNumber = rs.getString("homePhoneNumber");
			String workPhoneNumber = rs.getString("workPhoneNumber");
			String mobileNumber = rs.getString("mobileNumber");
			String email = rs.getString("email");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			Long preferredPlan = rs.getLong("preferredPlan");
			Date preferredCallingTime = rs.getTimestamp("preferredCallingTime");
			String note = rs.getString("note");
			String address = rs.getString("address");
			String streetArea = rs.getString("streetArea");
			String cityDistrict = rs.getString("cityDistrict");
			String state = rs.getString("state");
			String country = rs.getString("country");
			String status = rs.getString("status");
			String statusRemark = rs.getString("statusRemark");
			String zipCode = rs.getString("zipCode");
			String isDeleted = rs.getString("isDeleted");
			
			return new ClientProspectData(id, prospectType, firstName,
					middleName, lastName, homePhoneNumber, workPhoneNumber,
					mobileNumber, email, sourceOfPublicity,
					preferredCallingTime, note, address, streetArea,
					cityDistrict, state, country, preferredPlan, status,
					statusRemark, isDeleted, zipCode);
		}

		public String query() {
			
			return " p.id as id, p.prospect_type as prospectType, p.zip_code as zipCode, p.first_name as firstName," +
					" p.middle_name as middleName, p.last_name as lastName, p.home_phone_number as homePhoneNumber, " +
					" p.work_phone_number as workPhoneNumber, p.mobile_number as mobileNumber, p.email as email, " +
					" p.source_of_publicity as sourceOfPublicity, p.preferred_plan as preferredPlan," +
					" p.preferred_calling_time as preferredCallingTime, p.address as address," +
					" p.street_area as streetArea, p.city_district as cityDistrict, p.state as state," +
					" p.country as country, p.status as status, p.status_remark as statusRemark," +
					" p.is_deleted as isDeleted, p.note as note";
		}
	}

	@Override
	public ProspectPlanCodeData retriveClientProspectOrder(Long clientProspectId) {
		
		context.authenticatedUser();
		final ClientProspectOrderMapper rowMapper = new ClientProspectOrderMapper();
		final String sql = "select plan_id as planId, contract_period as contractPeriod, payterm_code as payTermCode, " +
				" no_of_connections as noOfConnections,price as amount from b_prospect_orders where prospect_id = ?";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { clientProspectId });
	}
	
	private static final class ClientProspectOrderMapper implements
	RowMapper<ProspectPlanCodeData> {

		@Override
		public ProspectPlanCodeData mapRow(final ResultSet rs, final int rowNum)
		throws SQLException {

			Long planId = rs.getLong("planId");
			Long contractPeriod = rs.getLong("contractPeriod");
			String payTermCode = rs.getString("payTermCode");
			Long noOfConnections = rs.getLong("noOfConnections");
			BigDecimal amount = rs.getBigDecimal("amount");
			return new ProspectPlanCodeData(planId, contractPeriod, payTermCode, noOfConnections, amount);

		}

	}
	
	@Override
	public ClientProspectCardDetailsData retriveclientProspectCardDetailsData(Long prospectId) {

		context.authenticatedUser();
		final ClientProspectCardDetailsMapper rowMapper = new ClientProspectCardDetailsMapper();
		final String sql = "select " + rowMapper.query()
				+ " where prospect_id = ? and is_deleted = 0 ";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { prospectId });
	}

	public class ClientProspectCardDetailsMapper implements RowMapper<ClientProspectCardDetailsData> {

		public String query() {

			return  " pcd.id as id, pcd.card_number as cardNumber, pcd.card_type as cardType, pcd.cvv_number as cvvNumber, " +
					" pcd.card_expiry_date as cardExpiryDate " +
					" from b_prospect_card_details pcd ";
		}

		@Override
		public ClientProspectCardDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String cardNumber = rs.getString("cardNumber");
			final String cardType = rs.getString("cardType");
			final String cvvNumber = rs.getString("cvvNumber");
			final String cardExpiryDate = rs.getString("cardExpiryDate");

			return new ClientProspectCardDetailsData(id, cardNumber, cardType, cvvNumber, cardExpiryDate);
		}

	}
	
	@Override
	public String getFileName(Long prospectId) {

		context.authenticatedUser();
		final GetFileNameMapper rowMapper = new GetFileNameMapper();
		final String sql = "select " + rowMapper.query()
				+ " where p.id = ? and is_deleted = 0 ";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { prospectId });
	}

	public class GetFileNameMapper implements RowMapper<String> {

		public String query() {

			return  " p.fileName as printFileName from b_prospect p ";
		}

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			final String printFileName = rs.getString("printFileName");

			return printFileName ;
		}

	}

	
}
