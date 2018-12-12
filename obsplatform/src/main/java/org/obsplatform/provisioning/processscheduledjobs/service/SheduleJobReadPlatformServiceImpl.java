package org.obsplatform.provisioning.processscheduledjobs.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EvoBatchData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.ScheduleJobData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.WorldpayBatchData;
import org.obsplatform.scheduledjobs.scheduledjobs.domain.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SheduleJobReadPlatformServiceImpl implements SheduleJobReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public SheduleJobReadPlatformServiceImpl(final RoutingDataSource dataSource) {

		  this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	@Override
	public List<ScheduleJobData> retrieveSheduleJobParameterDetails(final String paramValue) {
		
		try {
			final SheduleJobMapper mapper = new SheduleJobMapper();
			final String sql = "select " + mapper.sheduleLookupSchema()+ "where sr.report_name=?";
			return jdbcTemplate.query(sql, mapper, new Object[] { paramValue });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class SheduleJobMapper implements RowMapper<ScheduleJobData> {

		public String sheduleLookupSchema() {
			return "  sr.id as id,sr.report_name as batchName,sr.report_sql as query from stretchy_report sr ";
		}

		@Override
		public ScheduleJobData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String batchName = rs.getString("batchName");
			final String query = rs.getString("query");

			return new ScheduleJobData(id, batchName, query);
		}
	}

	@Override
	public List<Long> getClientIds(String query, final JobParameterData data) {
		
		try {
			final ClientIdMapper mapper = new ClientIdMapper();
			if ("Y".equalsIgnoreCase(data.isDynamic())) {
				return this.jdbcTemplate.query(query, mapper, new Object[] {});
			} else {
				query = query.toLowerCase().replace("now()","'"+ (data.getProcessDate() != null ? data.getProcessDate() : data.getDueDate()).toString() + "'");
				return this.jdbcTemplate.query(query, mapper, new Object[] {});
			}
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientIdMapper implements RowMapper<Long> {
		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			return rs.getLong("clientId");
			
		}
	}

	@Override
	public Long getMessageId(final String messageTemplateName) {

		try {
			final MessageTemplateMapper mapper = new MessageTemplateMapper();
			final String sql = "select " + mapper.getMessageTemplate();
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { messageTemplateName });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class MessageTemplateMapper implements RowMapper<Long> {

		public String getMessageTemplate() {
			
			return "mt.id as id from b_message_template mt where mt.template_description=?";

		}

		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			   return  rs.getLong("id");
		}
	}

	@Override
	public JobParameterData getJobParameters(final String jobName) {
		
		try {
			final List<JobParameters> jobParameters = this.retrieveJobParameters(jobName);
			return new JobParameterData(jobParameters);
		} catch (EmptyResultDataAccessException exception) {
			return null;
		}
	}

	private List<JobParameters> retrieveJobParameters(final String jobName) {
		
		try {
			final SheduleJobParametersMapper mapper = new SheduleJobParametersMapper();
			final String sql = "select " + mapper.jobParamLookUpSchema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { jobName });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class SheduleJobParametersMapper implements RowMapper<JobParameters> {
		
		public String jobParamLookUpSchema() {
			return "p.id as id,p.job_id as jobId,p.param_name as paramName,p.param_type as paramType,p.param_default_value as defaultValue,"
					+ "p.param_value as paramValue,p.is_dynamic as isDynamic,p.query_values as queryValue from job_parameters p, job j "
					+ "where j.id=p.job_id  and j.name=?";
		}

		@Override
		public JobParameters mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			final Long id = rs.getLong("id");
			final Long jobId = rs.getLong("jobId");
			final String paramType = rs.getString("paramType");
			final String paramName = rs.getString("paramName");
			final String defaultValue = rs.getString("defaultValue");
			final String paramValue = rs.getString("paramValue");
			final String isDynamic = rs.getString("isDynamic");
			final String queryValue = rs.getString("queryValue");
			return new JobParameters(id, jobId, paramName, paramType,defaultValue, paramValue, isDynamic, queryValue);
		}
	}

	@Override
	public List<ScheduleJobData> getJobQeryData() {
		
		try {
			final SheduleJobMapper mapper = new SheduleJobMapper();
			final String sql = "select " + mapper.sheduleLookupSchema()+ "where sr.report_category='Scheduling Job'";
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	@Override
	public String retrieveMessageData(final Long id) {
		
		try {
			final MessageMapper mapperdata = new MessageMapper();
			String query = mapperdata.retrieveId(id);
			return this.jdbcTemplate.queryForObject(query, mapperdata,new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class MessageMapper implements RowMapper<String> {
		
		private Long id;

		public String retrieveId(Long val) {
			this.id = val;
			return "SELECT sms_conf(" + val + ")";
		}

		@Override
		public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			String prepare = "sms_conf(" + id + ")";
			String message = rs.getString(prepare);
			return message;

		}
	}

	@Override
	public List<ScheduleJobData> retrieveSheduleJobDetails(final String paramValue) {

		try {

			//final ObsPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");
			//ThreadLocalContextUtil.setTenant(tenant);
			final SheduleJobMapper mapper = new SheduleJobMapper();
			final String sql = "select " + mapper.sheduleLookupSchema() + " where sr.report_name= ? ";
			return jdbcTemplate.query(sql, mapper, new Object[] { paramValue });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}


	@Override
	public List<Long> getBillIds(String query, final JobParameterData data) {
		
		try {
			final BillIdMapper mapper = new BillIdMapper();
			if ("Y".equalsIgnoreCase(data.isDynamic())) {
				return this.jdbcTemplate.query(query, mapper, new Object[] {});
			} else {
				query = query.replace("NOW() + INTERVAL 6 DAY","'" + data.getProcessDate() + "'");
				return this.jdbcTemplate.query(query, mapper, new Object[] {});
			}
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class BillIdMapper implements RowMapper<Long> {
		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			return rs.getLong("billId");
		}
	}

	@Override
	public List<Long> retrieveAddonsForDisconnection(final LocalDate processingDate) {

		final AddonMapper mapper = new AddonMapper();
		final String sql = "select  id from b_orders_addons od where Date_format(od.end_date, '%Y-%m-%d') < ? and od.status='ACTIVE'";
		return jdbcTemplate.query(sql, mapper,new Object[] { processingDate.toString() });
	}

	private static final class AddonMapper implements RowMapper<Long> {
		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			return rs.getLong("id");

		}
	}

	@Override
	public List<EvoBatchData> getUnProcessedRecurringData() {

		final SheduleJobEvoMapper mapper = new SheduleJobEvoMapper();
		final String sql = "select " + mapper.evoLookupSchema();
		return jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class SheduleJobEvoMapper implements RowMapper<EvoBatchData> {

		public String evoLookupSchema() {

			StringBuilder builder = new StringBuilder(
					" m.id as chargeId, m.client_id as clientId, m.Bill_date as date,")
					.append(" m.Due_amount as amount from b_bill_master m join ")
					.append(" b_clientuser cu ON m.client_id = cu.client_id and cu.is_auto_billing = 'Y' ")
					.append(" where m.id = (select max(id) from b_bill_master bm  where bm.client_id = m.client_id and bm.is_deleted = 'N')")
					.append(" and m.is_pay = 'N' having amount > 0");

			return builder.toString();
		}

		@Override
		public EvoBatchData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long chargeId = rs.getLong("chargeId");
			final Long clientId = rs.getLong("clientId");
			final BigDecimal amount = rs.getBigDecimal("amount");
			final LocalDate inVoiceDate = JdbcSupport.getLocalDate(rs, "date");

			return new EvoBatchData(chargeId, clientId, amount, inVoiceDate);
		}
	}

	@Override
	public List<UsageChargesData> getCustomerUsageRawData(final String query,JobParameterData data) {

		try {

			final UsageChargesMapper mapper = new UsageChargesMapper();
			return jdbcTemplate.query(query, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class UsageChargesMapper implements RowMapper<UsageChargesData> {

		@Override
		public UsageChargesData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long clientId = rs.getLong("clientId");
			final String number = rs.getString("number");

			return new UsageChargesData(clientId, number);

		}
	}

	@Override
	public List<Long> getClientIds(final String query) {
	
		final ClientIdMapper mapper = new ClientIdMapper();
		return jdbcTemplate.query(query, mapper, new Object[] {});
	}
	
	@Override
	public List<Long> getTicketIds(String query) {
		final TicketIdMapper mapper = new TicketIdMapper();
		return jdbcTemplate.query(query, mapper, new Object[] {});
	}
	
	private static final class TicketIdMapper implements RowMapper<Long> {
		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			return rs.getLong("ticketId");
			
		}
	}

	@Override
	public List<WorldpayBatchData> getUnProcessedWorldPayRecurringData() {
		final SheduleJobWorldPayMapper mapper = new SheduleJobWorldPayMapper();
		final String sql = "select " + mapper.worldpayLookupSchema();
		return jdbcTemplate.query(sql, mapper, new Object[] {});
		
	}
	private static final class SheduleJobWorldPayMapper implements RowMapper<WorldpayBatchData> {

		public String worldpayLookupSchema() {

			StringBuilder builder = new StringBuilder(
					" m.id as chargeId, m.client_id as clientId, m.Bill_date as date, wc.name as name,wc.w_token as w_token,wc.r_type as r_type, " +
					"wc.type as type, ")
					.append(" m.Due_amount as amount from b_bill_master m join ")
					.append(" b_clientuser cu ON m.client_id = cu.client_id and cu.is_worldpay_billing = 'Y' ")
					.append(" join b_client_balance cb ON cb.client_id=cu.client_id and cb.balance_amount > 0 ")
					.append(" join m_client_card_details wc ON m.client_id = wc.client_id and wc.r_type = 'RECURRING' and wc.type = 'ObfuscatedCard' and wc.is_deleted = 'N' ")
					.append(" where m.id = (select max(id) from b_bill_master bm  where bm.client_id = m.client_id and bm.is_deleted = 'N')")
					.append(" and m.is_pay = 'N' having amount > 0");

			return builder.toString();
		}

		@Override
		public WorldpayBatchData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long chargeId = rs.getLong("chargeId");
			final Long clientId = rs.getLong("clientId");
			final BigDecimal amount = rs.getBigDecimal("amount");
			final LocalDate inVoiceDate = JdbcSupport.getLocalDate(rs, "date");
			final String w_token = rs.getString("w_token");
			final String r_type =rs.getString("r_type");
			final String type =rs.getString("type");
			final String name=rs.getString("name");
			return new WorldpayBatchData(chargeId, clientId, amount, inVoiceDate,w_token,r_type,type,name);
		}
	}

	@Override
	public List<String> getClientData(String query) {
		
		final ClientDataMapper mapper = new ClientDataMapper();
		return jdbcTemplate.query(query, mapper, new Object[] {});
	
	}
	
	private static final class ClientDataMapper implements RowMapper<String> {
		@Override
		public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			return rs.getString("email");
			
		}
	}
}
