package org.obsplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.payterms.data.PaytermData;
import org.obsplatform.billing.planprice.service.PriceReadPlatformService;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.order.data.OrderAddonsData;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.data.OrderDiscountData;
import org.obsplatform.portfolio.order.data.OrderHistoryData;
import org.obsplatform.portfolio.order.data.OrderLineData;
import org.obsplatform.portfolio.order.data.OrderPriceData;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.plan.data.PlanCodeData;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderReadPlatformServiceImpl implements OrderReadPlatformService

{

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private static PriceReadPlatformService priceReadPlatformService;

	@Autowired
	public OrderReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
			final PriceReadPlatformService priceReadPlatformService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		OrderReadPlatformServiceImpl.priceReadPlatformService = priceReadPlatformService;

	}

	@Override
	public List<PlanCodeData> retrieveAllPlatformData(Long planId, Long clientId) {
		context.authenticatedUser();
		String sql = null;
		if (clientId != null) {
			/*
			 * sql =
			 * " SELECT s.id AS id, s.plan_code AS planCode, s.is_prepaid AS isPrepaid"
			 * +
			 * "  FROM b_plan_master s, b_plan_pricing p,b_priceregion_master prd,  b_priceregion_detail pd, b_client_address cd,b_state bs"
			 * +
			 * "  WHERE s.plan_status = 1 AND s.is_deleted = 'n' AND s.id != ?  AND  prd.id = pd.priceregion_id and  p.price_region_id = pd.priceregion_id"
			 * +
			 * "  and (pd.state_id =bs.id or (pd.state_id =0 and (pd.country_id = bs.parent_code or (pd.country_id = 0 and prd.priceregion_code ='Default'))))"
			 * + "  and s.id=p.plan_id" + "  and cd.client_id ="+clientId+
			 * " group by s.id";
			 */

			sql = "SELECT s.id AS id, s.plan_code AS planCode, s.plan_description as planDescription, s.is_prepaid AS isPrepaid"
					+ " FROM b_plan_pricing p,b_priceregion_master prd,b_priceregion_detail pd,b_client_address cd,b_state bs , " +
					" m_client m join b_plan_category_detail pcd ON pcd.client_category_id = m.category_type " +
					" join b_plan_master s ON (s.id = pcd.plan_id AND s.is_deleted = 'N') "
					+ " WHERE  s.plan_status = 1 AND s.is_deleted = 'n' AND m.id = " + clientId + " AND s.id != " + planId
					+ "  AND prd.id = pd.priceregion_id AND p.price_region_id = pd.priceregion_id AND "
					+ " (pd.state_id = ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a,b_priceregion_detail b,b_state c,b_charge_codes cc,b_client_address d"
					+ " WHERE  b.priceregion_id = a.price_region_id AND b.state_id = c.id AND a.price_region_id = b.priceregion_id AND d.state = c.state_name "
					+ " AND cc.charge_code = a.charge_code AND cc.charge_code = p.charge_code AND d.address_key = 'PRIMARY' AND d.client_id = "
					+ clientId + " " + "  AND a.plan_id != " + planId
					+ " AND a.is_deleted = 'n'),0) AND pd.country_id in ((SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_country c,"
					+ " b_charge_codes cc,b_client_address d WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND cc.charge_code = a.charge_code"
					+ " AND cc.charge_code = p.charge_code AND a.price_region_id = b.priceregion_id AND c.country_name = d.country AND d.address_key = 'PRIMARY'"
					+ " AND d.client_id = " + clientId
					+ "  AND a.plan_id != ? AND a.is_deleted = 'n'),0)) AND s.id = p.plan_id AND cd.client_id = "
					+ clientId + "  GROUP BY s.id";

		} else {
			sql = "  select s.id as id,s.plan_code as planCode,s.plan_description as planDescription,s.is_prepaid as isPrepaid "
					+ "  from b_plan_master s " + "  	where s.plan_status=1 and  s.is_deleted='n'  and s.id !=? ";
		}

		RowMapper<PlanCodeData> rm = new PeriodMapper();
		return this.jdbcTemplate.query(sql, rm, new Object[] { planId });
	}

	private static final class PeriodMapper implements RowMapper<PlanCodeData> {

		@Override
		public PlanCodeData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			String planCode = rs.getString("planCode");
			String planDescription = rs.getString("planDescription");
			String isPrepaid = rs.getString("isPrepaid");
			List<ServiceData> services = priceReadPlatformService.retrieveServiceDetails(id);
			return new PlanCodeData(id, planCode, services, isPrepaid, planDescription);

		}

	}

	@Override
	public List<PaytermData> retrieveAllPaytermData() {

		context.authenticatedUser();

		String sql = "select s.id as id,s.paymode_code as payterm_type,s.paymode_description as units from b_paymodes s";
		RowMapper<PaytermData> rm = new PaytermMapper();
		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	private static final class PaytermMapper implements RowMapper<PaytermData> {

		@Override
		public PaytermData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String paytermtype = rs.getString("payterm_type");
			final String units = rs.getString("units");
			final String data = units.concat(paytermtype);

			return new PaytermData(id, data, null, null, null, null,null);
		}
	}

	@Override
	public List<OrderPriceData> retrieveOrderPriceData(Long orderId) {
		context.authenticatedUser();

		/*
		 * String sql =
		 * "select s.id as id,s.order_id as order_id,s.charge_code as charge_code,s.service_id as service_id,s.charge_type as charge_type,s.charge_duration as charge_duration,"
		 * +
		 * "s.duration_type as duration_type,s.price as price from order_price s where s.order_id = ?"
		 * ;
		 */
		RowMapper<OrderPriceData> rm = new OrderPriceMapper();
		final OrderPriceMapper orderPriceMapper = new OrderPriceMapper();
		String sql = "select " + orderPriceMapper.schema();
		return this.jdbcTemplate.query(sql, rm, new Object[] { orderId });
	}

	private static final class OrderPriceMapper implements RowMapper<OrderPriceData> {
		public String schema() {
			return "s.id as id,s.order_id as order_id,s.charge_code as charge_code,s.service_id as service_id,s.charge_type as charge_type,s.charge_duration as charge_duration,"
					+ "s.duration_type as duration_type,s.price as price from b_order_price s where s.order_id = ?";

		}

		@Override
		public OrderPriceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			Long orderId = rs.getLong("order_id");
			Long serviceId = rs.getLong("service_id");
			String chargeCode = rs.getString("charge_code");
			String chargeType = rs.getString("charge_type");
			String chargeDuration = rs.getString("charge_duration");
			String durationtype = rs.getString("duration_type");
			BigDecimal price = rs.getBigDecimal("price");
			return new OrderPriceData(id, orderId, serviceId, chargeCode, chargeType, chargeDuration, durationtype,
					price, null, null, null, null, null, null);
		}

	}

	@Override
	public List<PaytermData> getChargeCodes(final Long planCode, final Long clientId) {

		context.authenticatedUser();

		String sql = " SELECT DISTINCT b.billfrequency_code AS billfrequencyCode,a.id AS id,c.id as contractPeriodId,c.contract_period AS duration,pm.is_prepaid AS isPrepaid,a.price as price,cv.chargevariant_code AS chargeVariant "
				+ " FROM b_charge_codes b, b_plan_master pm,b_plan_pricing a LEFT JOIN b_contract_period c ON c.contract_period = a.duration LEFT JOIN b_chargevariant cv ON a.charging_variant = cv.id "
				+ " WHERE  a.charge_code = b.charge_code AND a.is_deleted = 'n' AND a.plan_id = ? AND pm.id = a.plan_id";

		if (clientId != null) {

			sql = " SELECT DISTINCT b.billfrequency_code AS billfrequencyCode,a.id AS id,c.id as contractPeriodId,c.contract_period AS duration,pm.is_prepaid AS isPrepaid,a.price AS price,cv.chargevariant_code AS chargeVariant "
					+ " FROM b_charge_codes b,b_plan_master pm,b_plan_pricing a LEFT JOIN b_contract_period c ON c.contract_period = a.duration "
					+ " LEFT JOIN b_chargevariant cv ON a.charging_variant = cv.id LEFT JOIN b_priceregion_detail pd ON pd.priceregion_id = a.price_region_id "
					+ " JOIN b_client_address ca LEFT JOIN b_state s ON ca.state = s.state_name LEFT JOIN b_country con ON ca.country = con.country_name"
					+ " WHERE   a.charge_code = b.charge_code AND a.is_deleted = 'n' AND b.billfrequency_code <> 'Once' AND (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a,"
					+ " b_priceregion_detail b,b_state c, b_charge_codes cc, b_client_address d  WHERE b.priceregion_id = a.price_region_id AND b.state_id = c.id AND a.price_region_id = b.priceregion_id AND d.state = c.state_name "
					+ " AND cc.charge_code = a.charge_code AND cc.charge_code = b.charge_code AND d.address_key = 'PRIMARY' AND d.client_id = "
					+ clientId + " " + " AND a.plan_id = " + planCode
					+ " and a.is_deleted = 'n'),0) AND pd.country_id =ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a,b_priceregion_detail b,b_country c, b_charge_codes cc,b_client_address d"
					+ " WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND cc.charge_code = a.charge_code AND cc.charge_code = b.charge_code AND a.price_region_id = b.priceregion_id"
					+ " AND c.country_name = d.country AND d.address_key = 'PRIMARY' AND d.client_id =" + clientId
					+ " AND a.plan_id =" + planCode + " and a.is_deleted = 'n'),0)) "
					+ " AND a.plan_id =?  AND pm.id = a.plan_id group by b.billfrequency_code";
		}

		RowMapper<PaytermData> rm = new BillingFreaquencyMapper();
		return this.jdbcTemplate.query(sql, rm, new Object[] { planCode });
	}

	private static final class BillingFreaquencyMapper implements RowMapper<PaytermData> {

		@Override
		public PaytermData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String billfrequencyCode = rs.getString("billfrequencyCode");
			final String duration = rs.getString("duration");
			final String isPrepaid = rs.getString("isPrepaid");
			final BigDecimal price = rs.getBigDecimal("price");
			final String chargeVariant = rs.getString("chargeVariant");
			final Long contractPeriodId = rs.getLong("contractPeriodId");

			return new PaytermData(id, billfrequencyCode, duration, isPrepaid, price, chargeVariant, contractPeriodId);
		}
	}

	@Override
	public List<OrderPriceData> retrieveOrderPriceDetails(Long orderId, Long clientId) {

		RowMapper<OrderPriceData> rm = new OrderPriceDataMapper();

		String sql = "SELECT p.id AS id,o.client_id AS clientId,p.order_id AS order_id,c.charge_description AS chargeDescription,"
				+ "  if( p.service_id =0,'None',s.service_description) AS serviceDescription,p.charge_type AS charge_type,p.charge_duration AS chargeDuration, p.duration_type AS durationType,"
				+ "  p.price AS price,p.bill_start_date as billStartDate,p.bill_end_date as billEndDate,p.next_billable_day as nextBillableDay,p.invoice_tilldate as invoiceTillDate,"
				+ "  o.billing_align as billingAlign, o.billing_frequency as billingFrequency FROM b_order_price p,b_charge_codes c,b_service s, b_orders o "
				+ "  where p.charge_code = c.charge_code AND (p.service_id = s.id or p.service_id=0) AND o.id = p.order_id AND p.is_deleted <>'Y' "
				+ "  AND p.order_id = ? group by p.id";

		return this.jdbcTemplate.query(sql, rm, new Object[] { orderId });
	}

	private static final class OrderPriceDataMapper implements RowMapper<OrderPriceData> {

		@Override
		public OrderPriceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			Long orderId = rs.getLong("order_id");
			Long clientId = rs.getLong("clientId");
			String serviceDesciption = rs.getString("serviceDescription");
			String chargeDescription = rs.getString("chargeDescription");
			String chargeDuration = rs.getString("chargeDuration");
			String durationtype = rs.getString("durationType");
			String billingAlign = rs.getString("billingAlign");
			String billingFrequency = rs.getString("billingFrequency");
			BigDecimal price = rs.getBigDecimal("price");
			LocalDate billStartDate = JdbcSupport.getLocalDate(rs, "billStartDate");
			LocalDate billEndDate = JdbcSupport.getLocalDate(rs, "billEndDate");
			LocalDate nextBillDate = JdbcSupport.getLocalDate(rs, "nextBillableDay");
			LocalDate invoiceTillDate = JdbcSupport.getLocalDate(rs, "invoiceTillDate");

			return new OrderPriceData(id, orderId, clientId, serviceDesciption, chargeDescription, chargeDuration,
					durationtype, price, billStartDate, billEndDate, nextBillDate, invoiceTillDate, billingAlign,
					billingFrequency);
		}
	}

	@Override
	public List<OrderData> retrieveClientOrderDetails(Long clientId) {
		
		try {
			final ClientOrderMapper mapper = new ClientOrderMapper(); 

			final String sql = "select " + mapper.clientOrderLookupSchema()
					+ " where o.plan_id = p.id and o.client_id= ? and o.is_deleted='n' and o.contract_period = co.id AND c.id=o.client_id group by o.id desc";

			return jdbcTemplate.query(sql, mapper, new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientOrderMapper implements RowMapper<OrderData> {

		public String clientOrderLookupSchema() {
			return " o.id AS id,o.plan_id AS plan_id, ex.is_ex_directory AS isExDirectory, ex.is_number_with_held AS isNumberWithHeld, " 
		            + " ex.is_umee_app AS isUmeeApp, o.start_date AS start_date,o.order_status AS order_status, " 
					+ " o.connection_type as connectiontype,o.auto_renew as autoRenew,p.plan_code AS plan_code,"
					+ " o.end_date AS end_date,co.contract_period as contractPeriod,o.order_no as orderNo,o.order_no as orderId,o.user_action AS userAction,o.active_date AS activeDate,"
					+ " p.is_prepaid as isprepaid,p.allow_topup as allowTopUp, ba.hw_serial_no as hwSerialNo, ifnull(g.group_name, p.plan_code) as groupName,  "
					+ " date_sub(o.next_billable_day,INTERVAL 1 DAY) as invoiceTillDate,(SELECT sum(ol.price) AS price FROM b_order_price ol"
					+ " WHERE o.id = ol.order_id AND charge_type <> 'NRC' AND ol.is_deleted<>'Y')  AS price,p.provision_sys as provSys  FROM b_orders o " 
					+ " left join b_association ba on (ba.order_id = o.id and ba.is_deleted = 'N') " 
					+ " left join b_exdirectory ex ON (ex.order_id = o.id and ex.is_deleted = 'N'), b_plan_master p,b_contract_period co, m_client c "
					+ "  left join b_group g on g.id=c.group_id ";
		}

		@Override
		public OrderData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long planId = rs.getLong("plan_id");
			final Long orderId = rs.getLong("orderId");
			final String plancode = rs.getString("plan_code");
			final String contractPeriod = rs.getString("contractPeriod");
			final int statusId = rs.getInt("order_status");
			LocalDate startDate = JdbcSupport.getLocalDate(rs, "start_date");
			LocalDate activaDate = JdbcSupport.getLocalDate(rs, "activeDate");
			LocalDate endDate = JdbcSupport.getLocalDate(rs, "end_date");
			LocalDate invoiceTillDate = JdbcSupport.getLocalDate(rs, "invoiceTillDate");
			final double price = rs.getDouble("price");
			final String isprepaid = rs.getString("isprepaid");
			final String allowtopup = rs.getString("allowTopUp");
			final String userAction = rs.getString("userAction");
			final String provSys = rs.getString("provSys");
			final String orderNo = rs.getString("orderNo");
			final String groupName = rs.getString("groupName");
			final String autoRenew = rs.getString("autoRenew");
			final String connectiontype = rs.getString("connectiontype");
			EnumOptionData Enumstatus = OrderStatusEnumaration.OrderStatusType(statusId);
			String status = Enumstatus.getValue();
			final Boolean isExDirectory = rs.getBoolean("isExDirectory");
			final Boolean isNumberWithHeld = rs.getBoolean("isNumberWithHeld");
			final Boolean isUmeeApp = rs.getBoolean("isUmeeApp");
			final String hwSerialNo = rs.getString("hwSerialNo");

			return new OrderData(id, planId, plancode, status, startDate, endDate, price, contractPeriod, isprepaid,
					allowtopup, userAction, provSys, orderNo, invoiceTillDate, activaDate, groupName, autoRenew,
					connectiontype, orderId, isExDirectory, isNumberWithHeld, hwSerialNo, isUmeeApp);
		}
	}

	@Override
	public List<OrderHistoryData> retrieveOrderHistoryDetails(String orderNo) {

		try {
			final OrderHistoryMapper mapper = new OrderHistoryMapper();
			final String sql = "select " + mapper.clientOrderLookupSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] { orderNo });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class OrderHistoryMapper implements RowMapper<OrderHistoryData> {

		public String clientOrderLookupSchema() {
			return "  h.id AS id,h.transaction_date AS transDate,h.actual_date AS actualDate,h.transaction_type AS transactionType,"
					+ " h.prepare_id AS PrepareRequsetId, ifnull(a.username,'by Scheduler job') as userName  FROM b_orders_history h"
					+ "  left join m_appuser a on a.id=h.createdby_id WHERE h.order_id = ? ";
		}

		@Override
		public OrderHistoryData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final LocalDate transDate = JdbcSupport.getLocalDate(rs, "transDate");
			final LocalDate actualDate = JdbcSupport.getLocalDate(rs, "actualDate");
			final LocalDate provisionongDate = JdbcSupport.getLocalDate(rs, "actualDate");
			final String transactionType = rs.getString("transactionType");
			final Long PrepareRequsetId = rs.getLong("PrepareRequsetId");
			final String userName = rs.getString("userName");

			return new OrderHistoryData(id, transDate, actualDate, provisionongDate, transactionType, PrepareRequsetId,
					userName);
		}
	}

	@Override
	public List<OrderData> getActivePlans(Long clientId, String planType) {

		try {
			final ActivePlanMapper mapper = new ActivePlanMapper();

			String sql = null;
			if (planType != null) {
				if (planType.equalsIgnoreCase("prepaid")) {
					sql = "select " + mapper.activePlanLookupSchema() + " AND p.is_prepaid = 'Y'";
				} else {
					sql = "select " + mapper.activePlanLookupSchema() + " AND p.is_prepaid = 'N'";
				}
			} else {
				sql = "select " + mapper.activePlanLookupSchema();
			}

			return jdbcTemplate.query(sql, mapper, new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ActivePlanMapper implements RowMapper<OrderData> {

		public String activePlanLookupSchema() {
			return "o.id AS orderId,p.plan_code AS planCode,p.plan_description as planDescription,o.billing_frequency AS billingFreq,"
					+ "o.end_date as endDate,c.contract_period as contractPeriod,(SELECT sum(ol.price) AS price FROM b_order_price ol"
					+ " WHERE o.id = ol.order_id)  AS price  FROM b_orders o, b_plan_master p, b_contract_period c WHERE client_id =?"
					+ " AND p.id = o.plan_id  and o.contract_period=c.id and o.order_status=1 and o.is_deleted ='N' ";
		}

		@Override
		public OrderData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long orderId = rs.getLong("orderId");
			final String planCode = rs.getString("planCode");
			final String planDescription = rs.getString("planDescription");
			final String billingFreq = rs.getString("billingFreq");
			final String contractPeriod = rs.getString("contractPeriod");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
			final Double price = rs.getDouble("price");

			return new OrderData(orderId, planCode, planDescription, billingFreq, contractPeriod, price, endDate);
		}
	}

	@Override
	public OrderData retrieveOrderDetails(Long orderId) {
		try {
			final ClientOrderMapper mapper = new ClientOrderMapper();
			final String sql = "select " + mapper.clientOrderLookupSchema()
					+ " where o.plan_id = p.id and o.id=? and o.is_deleted='n'"
					+ " and o.contract_period = co.id  and  c.id=o.client_id group by o.id desc";

			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { orderId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	@Override
	public Long getRetrackId(Long id) {
		try {

			final String sql = "select MAX(h.id) as id from b_orders_history h where h.order_id=? and h.transaction_type LIKE '%tion%'";
			RowMapper<Long> rm = new OSDMapper();
			return jdbcTemplate.queryForObject(sql, rm, new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class OSDMapper implements RowMapper<Long> {

		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			return rs.getLong("id");
			
		}
	}

	@Override
	public String getOSDTransactionType(Long id) {
		try {

			final String sql = "select h.transaction_type as type from b_orders_history h where h.id=?";
			RowMapper<String> rm = new OSDMapper1();
			return jdbcTemplate.queryForObject(sql, rm, new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class OSDMapper1 implements RowMapper<String> {

		@Override
		public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final String type = rs.getString("type");
			return type;
		}
	}

	@Override
	public String checkRetrackInterval(final Long entityId) {

		final OSDMapper1 rm = new OSDMapper1();
		final String sql = "select if (max(created_date) < date_sub('" + DateUtils.getDateTimeOfTenant()
				+ "',INTERVAL 1 HOUR) , 'yes','no') as type"
				+ " from b_orders_history where transaction_type in ('ACTIVATION','DISCONNECTION','RECONNECTION')"
				+ " and order_id=?";
		return jdbcTemplate.queryForObject(sql, rm, new Object[] { entityId });
	}

	@Override
	public List<OrderLineData> retrieveOrderServiceDetails(Long orderId) {

		try {
			final ClientOrderServiceMapper mapper = new ClientOrderServiceMapper();
			final String sql = "SELECT " + mapper.orderServiceLookupSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] { orderId, orderId, orderId, orderId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOrderServiceMapper implements RowMapper<OrderLineData> {

		public String orderServiceLookupSchema() {

			return /*
					 * " ol.id AS id,s.id AS serviceId,ol.order_id AS orderId,s.service_code AS serviceCode,s.is_auto AS isAuto,"
					 * +
					 * " s.service_description AS serviceDescription,s.service_type AS serviceType,psd.image AS image "
					 * +
					 * " FROM b_order_line ol, b_service s left join b_prov_service_details psd on  psd.service_id = s.id "
					 * +
					 * " WHERE  order_id = ? AND ol.service_id = s.id AND ol.is_deleted = 'N'"
					 * ;
					 */
			" ol.id AS id,s.id AS serviceId,o.plan_id as planId,ol.order_id AS orderId,s.service_code AS serviceCode,s.is_auto AS isAuto,"
					+ " s.service_description AS serviceDescription,s.service_type AS serviceType,psd.image AS image,"
					+ " ass.associateId as associateId,ass.allocationType AS allocationType,ass.serialNo AS serialNo,ass.itemId as itemId "
					+ " FROM b_orders o JOIN b_order_line ol ON o.id=ol.order_id "
					+ " JOIN b_service s ON ol.service_id = s.id AND ol.is_deleted ='N' "
					+ " LEFT JOIN b_prov_service_details psd ON  psd.service_id = s.id AND psd.is_deleted='n' "
					+ " LEFT JOIN "
					+ " (SELECT pa.id as associateId,pa.order_id as orderId,pa.plan_id AS planId,pa.service_id AS serviceId,"
					+ " pa.allocation_type AS allocationType,pa.hw_serial_no AS serialNo,i.id AS itemId "
					+ " FROM b_association pa, b_order_line ol,b_plan_master p,b_item_detail id,b_item_master i,b_onetime_sale os "
					+ /* plan level */
					" WHERE pa.order_id=ol.order_id AND id.serial_no = pa.hw_serial_no AND pa.plan_id=p.id "
					+ " AND id.item_master_id = i.id AND os.item_id =i.id AND os.client_id = pa.client_id  "
					+ " AND pa.is_deleted='N' AND pa.order_id = ? AND pa.service_id IS NULL" + " UNION "
					+ " SELECT pa.id as associateId,pa.order_id as orderId,pa.plan_id AS planId,pa.service_id as serviceId,"
					+ " pa.allocation_type AS allocationType,pa.hw_serial_no AS serialNo,i.id AS itemId "
					+ " FROM b_association pa, b_order_line ol,b_plan_master p,b_owned_hardware o,b_item_master i "
					+ /* own hardware */
					" WHERE pa.order_id=ol.order_id AND o.serial_number = pa.hw_serial_no AND pa.plan_id=p.id "
					+ " AND o.item_type=i.id AND pa.is_deleted='N' AND pa.service_id IS NULL AND pa.order_id = ? "
					+ " UNION "
					+ " SELECT sa.id as associateId,sa.order_id as orderId,sa.plan_id AS planId,sa.service_id as serviceId,"
					+ " sa.allocation_type AS allocationType,sa.hw_serial_no AS serialNo,i.id AS itemId "
					+ " FROM b_association sa, b_order_line ol,b_plan_master p,b_item_detail id,b_item_master i,b_onetime_sale os "
					+ /* service level */
					" WHERE sa.order_id=ol.order_id AND id.serial_no = sa.hw_serial_no AND sa.plan_id=p.id "
					+ " AND id.item_master_id = i.id AND os.item_id =i.id AND os.client_id = sa.client_id "
					+ " AND sa.is_deleted='N' AND sa.service_id=ol.service_id  AND sa.service_id IS NOT NULL "
					+ " AND sa.order_id = ?  GROUP BY sa.service_id) AS ass "
					+ " ON ass.orderId=ol.order_id AND ifnull(ass.serviceId=ol.service_id,o.plan_id=ass.planId) "
					+ " WHERE ol.order_id = ?  GROUP BY ol.service_id";

		}

		@Override
		public OrderLineData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long orderId = rs.getLong("orderId");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceDescription = rs.getString("serviceDescription");
			final String serviceType = rs.getString("serviceType");
			final Long serviceId = rs.getLong("serviceId");
			final String isAutoProvision = rs.getString("isAuto");
			final String image = rs.getString("image");
			final Long associateId = rs.getLong("associateId");
			final String serialNo = rs.getString("serialNo");
			final String allocationType = rs.getString("allocationType");
			final Long itemId = rs.getLong("itemId");
			return new OrderLineData(id, orderId, serviceCode, serviceDescription, serviceType, serviceId,
					isAutoProvision, image, associateId, serialNo, allocationType, itemId);
		}
	}

	@Override
	public List<OrderDiscountData> retrieveOrderDiscountDetails(Long orderId) {

		try {
			final ClientOrderDiscountMapper mapper = new ClientOrderDiscountMapper();
			final String sql = "select " + mapper.orderDiscountLookupSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] { orderId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOrderDiscountMapper implements RowMapper<OrderDiscountData> {

		public String orderDiscountLookupSchema() {
			return "od.id as id,od.orderprice_id as priceId,od.discount_rate as discountAmount,od.discount_code as discountCode,"
					+ "d.discount_description as discountDescription,od.discount_type as discountType,od.discount_startdate as startDate,"
					+ " od.discount_enddate as endDate  FROM b_order_discount od, b_discount_master d"
					+ " where od.discount_id=d.id and od.is_deleted='N' and od.order_id=?";
		}

		@Override
		public OrderDiscountData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long priceId = rs.getLong("priceId");
			final String discountCode = rs.getString("discountCode");
			final String discountdescription = rs.getString("discountDescription");
			final String discountType = rs.getString("discountType");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
			final BigDecimal discountAmount = rs.getBigDecimal("discountAmount");
			return new OrderDiscountData(id, priceId, discountCode, discountdescription, discountAmount, discountType,
					startDate, endDate);
		}
	}

	@Override
	public Long retrieveClientActiveOrderDetails(Long clientId, String serialNo) {

		try {
			final ClientActiveOrderMapper mapper = new ClientActiveOrderMapper();
			String sql = null;
			if (serialNo != null) {
				sql = "select " + mapper.activeOrderLookupSchemaForAssociation()
						+ "  and a.is_deleted = 'N' and a.hw_serial_no='" + serialNo + "'";
			} else {
				sql = "select " + mapper.activeOrderLookupSchema();
			}
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientActiveOrderMapper implements RowMapper<Long> {

		public String activeOrderLookupSchemaForAssociation() {
			return " ifnull(max(o.id),0) as orders from b_orders o, b_association a where  o.id = a.order_id and o.client_id = ? "
					+ " and  o.order_status=1 ";
		}

		public String activeOrderLookupSchema() {
			return " count(*) AS orders FROM b_orders o WHERE o.client_id = ? AND o.order_status = 1 ";
		}

		@Override
		public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final Long activeOrdersCount = rs.getLong("orders");
			return activeOrdersCount;
		}
	}

	/**
	 * (non-Java doc)
	 * 
	 * @see retrieveCustomerActiveOrders(java.lang.Long)
	 */
	@Override
	public List<OrderData> retrieveCustomerActiveOrders(Long clientId) {

		final OrderMapper mapper = new OrderMapper();
		final String sql = "select id  from b_orders where order_status=1 and client_id=?";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	private static final class OrderMapper implements RowMapper<OrderData> {

		@Override
		public OrderData mapRow(final ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");

			return new OrderData(id);
		}
	}

	@Override
	public List<Long> retrieveOrderActiveAndDisconnectionIds(Long clientId, Long planId) {

		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select id from b_orders o where o.order_status in (1,3) and o.client_id=? and o.plan_id =? and o.is_deleted = 'N' order by o.order_status";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, planId });
	}

	private static final class OrderIdMapper implements RowMapper<Long> {

		@Override
		public Long mapRow(final ResultSet rs, int rowNum) throws SQLException {

			return rs.getLong("id");
		}

	}

	@Override
	public List<Long> getEventActionsData(Long clientId, Long orderId) {
		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select id from b_event_actions where event_action = 'CHANGEPLAN' and "
				+ "action_name = 'CHANGE PLAN' and client_id = ? and order_id = ? and "
				+ "is_processed = 'N' and trans_date >  " + DateUtils.getLocalDateOfTenant();
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, orderId });
	}

	@Override
	public List<OrderData> primaryOrderDetails(Long planId, Long clientId) {
		
		try {
			final ClientPrimaryOrderMapper mapper = new ClientPrimaryOrderMapper();
			final String sql = "select " + mapper.ClientPrimaryOrderLookupSchema()
					+ " where o.plan_id =? and o.client_id=? and (o.connection_type like 'PRIMARY' OR o.connection_type like 'REGULAR')";
			return jdbcTemplate.query(sql, mapper, new Object[] { planId, clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientPrimaryOrderMapper implements RowMapper<OrderData> {

		public String ClientPrimaryOrderLookupSchema() {
			return " o.id as orderid, o.connection_type as connectiontype from b_orders o ";
		}

		@Override
		public OrderData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("orderid");
			final String connectiontype = rs.getString("connectiontype");

			return new OrderData(id, connectiontype);
		}
	}

	@Override
	public List<EventMasterData> retrieveOrderEventDetails(Long orderId) {

		try {
			final ClientOrderEventMapper mapper = new ClientOrderEventMapper();
			final String sql = "select " + mapper.orderEventLookupSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] { orderId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOrderEventMapper implements RowMapper<EventMasterData> {

		public String orderEventLookupSchema() {
			return " ol.id AS id,s.id AS eventId,ol.order_id AS orderId,s.event_name AS eventName, "
					+ " s.event_description AS eventDescription FROM b_order_events ol, b_mod_master s  "
					+ " WHERE order_id = ? AND ol.event_id = s.id AND ol.is_deleted = 'N'";
		}

		@Override
		public EventMasterData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long orderId = rs.getLong("orderId");
			final String eventName = rs.getString("eventName");
			final String eventDescription = rs.getString("eventDescription");
			final Long eventId = rs.getLong("eventId");
			return new EventMasterData(id, orderId, eventName, eventDescription, eventId);
		}
	}

	@Override
	public List<Long> retrieveCustomerActiveOrderIds(final Long clientId) {

		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select b.id as id from b_orders b,b_order_price bp where b.id=bp.order_id and b.order_status=1 and client_id=?";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	@Override
	public List<Long> retrieveCustomerSuspendedOrderIds(final Long clientId) {

		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select b.id as id from b_orders b where b.order_status=6 and client_id=? "+
		                   "and b.id not in (select bea.order_id as orderId from b_event_actions bea where bea.client_id=b.client_id "+
                           "and bea.order_id=b.id and bea.entity_name='Order Reactivate' and bea.is_processed='N' "+ 
                           "and date_format(bea.trans_date, '%Y-%m-%d')<=date_format(now(), '%Y-%m-%d'))";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	@Override
	public List<Long> retrieveCustomerTalkSuspendedOrders(final Long clientId) {
		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select b.id as id from b_orders b,b_order_price bp where b.id=bp.order_id and bp.charge_type='UC' and b.order_status=6 and client_id=?";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	@Override
	public List<Long> retrieveCustomerTalkDisconnectedOrders(Long clientId) {
		final OrderIdMapper mapper = new OrderIdMapper();
		final String sql = "select b.id as id from b_orders b,b_order_price bp where b.id=bp.order_id and bp.charge_type='UC' and b.order_status=3 and client_id=? "+
		                   "and b.id not in (select bea.order_id as orderId from b_event_actions bea where bea.client_id=b.client_id "+
                           "and bea.order_id=b.id and bea.entity_name='Reconnection Service' and bea.is_processed='N' "+
                           "and date_format(bea.trans_date, '%Y-%m-%d')<=date_format(now(), '%Y-%m-%d')) "+
                           "union "+
                           "select max(b.id) as id from b_orders b,b_order_price bp where b.id=bp.order_id and charge_type='RC' and b.order_status=3 and client_id=? "+
                           "and b.id not in (select bea.order_id as orderId from b_event_actions bea where bea.client_id=b.client_id "+
                           "and bea.order_id=b.id and bea.entity_name='Reconnection Service' and bea.is_processed='N' "+
                           "and date_format(bea.trans_date, '%Y-%m-%d')<=date_format(now(), '%Y-%m-%d'))";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, clientId });
	}
	
	@Override
	public List<OrderAddonsData> getNewPlanAddon(Long planId) {

		try {
			final ClientOrderAddonMapper mapper = new ClientOrderAddonMapper();
			final String sql = "select distinct " + mapper.orderAddonSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] { planId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOrderAddonMapper implements RowMapper<OrderAddonsData> {

		public String orderAddonSchema() {
			return "  pm.plan_code as planName, cc.id as chargeCodeId, adp.service_id as serviceId, " +
					" adp.price as price from b_plan_master pm " +
					" join b_addons_service ads on (pm.id = ads.plan_id and ads.is_deleted = 'N') " +
					" join b_addons_service_price adp on (ads.id = adp.adservice_id and adp.is_deleted = 'N') " +
					" join b_charge_codes cc on (cc.charge_code = ads.charge_code) " +
					" join b_service s on (adp.service_id = s.id and s.is_optional = 'Y' and s.is_deleted = 'N') "+
					" where pm.id = ? and pm.is_deleted = 'N'";
		}

		@Override
		public OrderAddonsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long serviceId = rs.getLong("serviceId");
			final String planName = rs.getString("planName");
			final Long chargeCodeId = rs.getLong("chargeCodeId");
			//final Long contractId = rs.getLong("contractId");
			final BigDecimal price = rs.getBigDecimal("price");
			return new OrderAddonsData(serviceId, planName, chargeCodeId, price);
		}
	}
	
	@Override
	public OrderAddonsData retrieveTalkAddons(Long clientId, Long planId) {

		try {
			final ClientTalkOrderAddonMapper mapper = new ClientTalkOrderAddonMapper();
			final String sql = "select distinct " + mapper.talkOrderAddonSchema();
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { clientId, planId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientTalkOrderAddonMapper implements RowMapper<OrderAddonsData> {

		public String talkOrderAddonSchema() {
			return "  o.id as id , o.plan_id as planId, pm.plan_code as planName,s.id as serviceId,s.service_code as serviceName, oa.contract_id as contractId" +
					" from b_orders o join b_orders_addons oa on (o.id = oa.order_id and oa.is_deleted = 'N' and oa.status = 'ACTIVE' ) " +
					" join b_service s on (oa.service_id = s.id and s.is_deleted = 'N') " +
					" join b_plan_master pm on (o.plan_id = pm.id and pm.is_deleted = 'N') " +
					" where o.client_id = ? and o.plan_id = ? and o.is_deleted = 'N' ";
		}

		@Override
		public OrderAddonsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long planId = rs.getLong("planId");
			final String planName = rs.getString("planName");
			final Long serviceId = rs.getLong("serviceId");
			final String serviceName = rs.getString("serviceName");
			final Long contractId = rs.getLong("contractId");
			return new OrderAddonsData(id, planId, planName, serviceId, serviceName, contractId);
		}
	}
	
	
	@Override
	public List<OrderData> clientActiveOrderDetails( Long clientId) {
		
		try {
			final ClientActiveOrdersMapper mapper = new ClientActiveOrdersMapper();
			final String sql = "select " + mapper.ClientActiveOrderSchema();
			return jdbcTemplate.query(sql, mapper, new Object[] {  clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientActiveOrdersMapper implements RowMapper<OrderData> {

		public String ClientActiveOrderSchema() {
			return " bo.id as orderId, bo.plan_id as planId, bpm.plan_code as planName from b_orders  bo " +
					" join b_plan_master bpm on bpm.id = bo.plan_id where  client_id = ? and bo.is_deleted = 'N' ";
		}

		@Override
		public OrderData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("orderId");
			final Long planId = rs.getLong("planId");
			final String planName = rs.getString("planName");

			return new OrderData( id, planName, planId);
		}
	}
	
	@Override
	public PlanCodeData getPlanDetails( Long planId) {
		
		try {
			final ClientNewPlanOrdersMapper mapper = new ClientNewPlanOrdersMapper();
			final String sql = "select " + mapper.ClientNewPlanOrdersSchema();
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {  planId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientNewPlanOrdersMapper implements RowMapper<PlanCodeData> {

		public String ClientNewPlanOrdersSchema() {
			return " plan_code as planCode, provision_sys as provisionSystem from b_plan_master where id = ?";
		}

		@Override
		public PlanCodeData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final String planCode = rs.getString("planCode");

			return new PlanCodeData( null,planCode,null,null,null);
		}
	}
	
	
	@Override	
	public OrderPriceData findNewTalkAddonsPriceByTalkOrderId( Long orderId) {
		try {			
			final NewTalkAddonOrdersMapper mapper = new NewTalkAddonOrdersMapper();			
			final String sql = "select " + mapper.ClientNewPlanOrdersSchema();			
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {  orderId });
			//return jdbcTemplate.queryForObject(sql, mapper, new Object[] {  orderId });		
		} catch (EmptyResultDataAccessException e) {			
			return null;		
		}	
		
	}		
	private static final class NewTalkAddonOrdersMapper implements RowMapper<OrderPriceData> {		
		public String ClientNewPlanOrdersSchema() {			
			return "  id as orderPriceId, order_id as orderId from b_order_price where order_id =? and is_deleted = 'N' and charge_type = 'RC' ";		
		}
		
		@Override		
		public OrderPriceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {			
			final Long id = rs.getLong("orderPriceId");			
			final Long orderId = rs.getLong("orderId");			
			return new OrderPriceData( id, orderId);		
		}	
	}
	
	@Override	
	public OrderAddonsData findNewAddonsPriceByOrderId( Long orderId) {
		try {			
			final NewAddonOrdersMapper mapper = new NewAddonOrdersMapper();			
			final String sql = "select " + mapper.ClientNewPlanOrdersSchema();			
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {  orderId });
			//return jdbcTemplate.queryForObject(sql, mapper, new Object[] {  orderId });		
		} catch (EmptyResultDataAccessException e) {			
			return null;		
		}	
		
	}		
	private static final class NewAddonOrdersMapper implements RowMapper<OrderAddonsData> {		
		public String ClientNewPlanOrdersSchema() {			
			return "  order_id as orderId,price_id as orderPriceId from b_orders_addons where order_id =? and is_deleted = 'N' ";		
		}
		
		@Override		
		public OrderAddonsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {			
			final Long orderPriceId = rs.getLong("orderPriceId");			
			final Long orderId = rs.getLong("orderId");			
			return new OrderAddonsData( orderPriceId, orderId);		
		}	
	}


}
