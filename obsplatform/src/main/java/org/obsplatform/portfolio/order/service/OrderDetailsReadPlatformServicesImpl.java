package org.obsplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.billing.planprice.data.PriceData;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailsReadPlatformServicesImpl implements OrderDetailsReadPlatformServices{

	  private final JdbcTemplate jdbcTemplate;
	
 @Autowired

 public OrderDetailsReadPlatformServicesImpl(final RoutingDataSource dataSource) {
	        this.jdbcTemplate = new JdbcTemplate(dataSource);
	    }

	@Override
	public List<ServiceData> retrieveAllServices(Long plan_code) {

		PlanMapper mapper = new PlanMapper();
		String sql = "select " + mapper.schema() + " and da.plan_id = '" + plan_code + "'";
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});

	}

	private static final class PlanMapper implements RowMapper<ServiceData> {

		public String schema() {
			return "da.id as id,se.id as serviceId, da.service_code as service_code, da.plan_id as plan_code,se.service_type as serviceType "
					+" from b_plan_detail da,b_service se where da.service_code = se.service_code";

		}

		@Override
		public ServiceData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
			
			Long id = rs.getLong("id");
			String serviceCode = rs.getString("service_code");
			String serviceType = rs.getString("serviceType");
			Long serviceid = rs.getLong("serviceId");

			return new ServiceData(id,serviceid,serviceCode,null, null,null,null, null,serviceType,null);
	}
	}
	@Override
		public List<PriceData> retrieveAllPrices(Long planId,String billingFreq, Long clientId ) {


			PriceMapper mapper = new PriceMapper();

			/*String sql = "select " + mapper.schema()+" and da.plan_id = '"+plan_code+"' and (c.billfrequency_code='"+billingFreq+"'  or c.billfrequency_code='Once')" +
					" AND ca.client_id = ?  AND da.price_region_id =pd.priceregion_id AND s.state_name = ca.state And s.parent_code=pd.country_id" +
					" AND pd.state_id = s.id group by da.id";*/
			
			String sql ="SELECT da.id AS id,if(da.service_code = 'None', 0, se.id) AS serviceId,da.service_code AS service_code,da.charge_code AS charge_code," +

				     " da.charging_variant AS charging_variant,c.charge_type AS charge_type,c.charge_duration AS charge_duration,c.duration_type AS duration_type," +
				     " da.discount_id AS discountId,c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,s.id AS stateId,s.parent_code AS countryId," +
				     " pd.state_id AS regionState,con.country_name,pd.country_id AS regionCountryId" +
				     " FROM b_plan_pricing da LEFT JOIN b_charge_codes c ON c.charge_code = da.charge_code  " +
				     " LEFT JOIN b_service se ON (da.service_code = se.service_code OR da.service_code = 'None')" +
				     " LEFT JOIN b_priceregion_detail pd ON pd.priceregion_id = da.price_region_id" +
				     " JOIN b_client_address ca LEFT JOIN b_state s ON ca.state = s.state_name LEFT JOIN b_country con ON ca.country = con.country_name" +
				     " WHERE da.is_deleted = 'n' AND ca.address_key = 'PRIMARY' AND da.plan_id = ? AND (c.billfrequency_code =? OR c.billfrequency_code = 'Once') " +
				     " AND ca.client_id = ? AND (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_state c," +
				     " b_charge_codes cc,b_client_address d" +
				     " WHERE b.priceregion_id = a.price_region_id  AND cc.charge_code = a.charge_code AND  b.state_id = c.id   AND a.price_region_id = b.priceregion_id " +
				     " AND d.state = c.state_name AND d.address_key = 'PRIMARY' AND d.client_id = ? and a.plan_id=? AND (cc.billfrequency_code = ? OR cc.billfrequency_code = 'Once')),0)" +
				     " AND pd.country_id = ifnull( (SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_country c, b_charge_codes cc,b_client_address d " +
				     " WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND cc.charge_code = a.charge_code AND a.price_region_id = b.priceregion_id " +
				     " AND c.country_name = d.country AND d.address_key = 'PRIMARY' AND d.client_id = ? AND a.plan_id = ? AND (cc.billfrequency_code =  ? OR cc.billfrequency_code = 'Once' )),0))" +
				     " GROUP BY da.id";
			
			return this.jdbcTemplate.query(sql, mapper, new Object[] { planId,billingFreq,clientId,clientId,planId,billingFreq,clientId,planId,billingFreq});

		} 

		private static final class PriceMapper implements RowMapper<PriceData> {

			public String schema() {
				return " da.id AS id,if(da.service_code ='None',0, se.id) AS serviceId, da.service_code AS service_code,da.charge_code AS charge_code,da.charging_variant AS charging_variant," +
						"c.charge_type AS charge_type,c.charge_duration AS charge_duration,c.duration_type AS duration_type,da.discount_id AS discountId," +
						"c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,s.id AS stateId,s.parent_code AS countryId,pd.state_id AS regionState," +
						"pd.country_id AS regionCountryId FROM b_plan_pricing da,b_charge_codes c,b_service se,b_client_address ca,b_state s,b_country con,b_priceregion_detail pd,b_priceregion_master prd" +
						" WHERE  da.charge_code = c.charge_code  AND ( da.service_code = se.service_code or da.service_code ='None') AND da.is_deleted = 'n' AND ca.address_key='PRIMARY'" ;
					   

			}

			@Override
			public PriceData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

				Long id = rs.getLong("id");
				String serviceCode = rs.getString("service_code");
				String chargeCode = rs.getString("charge_code");
				String chargingVariant = rs.getString("charging_variant");
				BigDecimal price=rs.getBigDecimal("price");
				String chargeType = rs.getString("charge_type");
				String chargeDuration = rs.getString("charge_duration");
				String durationType = rs.getString("duration_type");
				Long serviceid = rs.getLong("serviceId");
				Long discountId=rs.getLong("discountId");
				Long stateId = rs.getLong("stateId");
				Long countryId=rs.getLong("countryId");
				Long regionState = rs.getLong("regionState");
				Long regionCountryId=rs.getLong("regionCountryId");
				boolean taxinclusive=rs.getBoolean("taxInclusive");
				
				return new PriceData(id, serviceCode, chargeCode,chargingVariant,price,chargeType,chargeDuration,durationType,
			              serviceid,discountId,taxinclusive,stateId,countryId,regionState,regionCountryId);

			}
	}
		
		@Override
		public List<PriceData> retrieveDefaultPrices(Long planId,String billingFrequency, Long clientId) {
			
			PriceMapper mapper1 = new PriceMapper();
			/*String sql = "select " + mapper1.schema()+" and da.plan_id = '"+planId+"' and (c.billfrequency_code='"+billingFrequency+"'  or c.billfrequency_code='Once')" +
					" AND ca.client_id = ?  AND da.price_region_id =pd.priceregion_id AND s.state_name = ca.state" +
					" And (pd.country_id =s.parent_code or (pd.country_id =0 and prd.priceregion_code ='Default'))" +
					" AND pd.state_id =0 group by da.id";*/
			String sql="select " + mapper1.schema()+" and da.plan_id ='"+planId+"' AND c.billfrequency_code = '"+billingFrequency+"' AND ca.client_id =?" +
					" AND da.price_region_id = pd.priceregion_id AND s.state_name = ca.state AND con.country_name=ca.country" +
					" AND pd.country_id=con.id and prd.id =pd.priceregion_id AND (pd.country_id = s.parent_code or (pd.country_id =0 and prd.priceregion_code ='default'))" +
					" AND pd.state_id = 0 GROUP BY da.id; ";
			return this.jdbcTemplate.query(sql, mapper1, new Object[] { clientId });
		}

	@Override
	public Long retrieveClientActivePlanOrdersCount(final Long clientId,final Long planId) {

		final String sql = "select count(*) from b_orders bo where bo.client_id = ? and bo.plan_id = ? and order_status in (1,4) and bo.is_deleted='n' ";

		return this.jdbcTemplate.queryForLong(sql, new Object[] { clientId,planId });

	}
	

	@Override
	public List<Long> retrieveDisconnectingOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status in (1,3) and bo.is_deleted='n' and bo.connection_type='SECONDARY' "
				+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { clientId, planId });
	}
	

	@Override
	public List<Long> retrieveTerminatableOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status<> 5 and user_action<>'TERMINATION' and bo.is_deleted='n' "
				+ " and bo.connection_type='SECONDARY' and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] {clientId, planId });
	}

	@Override
	public List<Long> retrieveSuspendableOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status = 1 and bo.is_deleted='n' "
				+ " and bo.connection_type='SECONDARY' and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { clientId, planId });
	}
	
	@Override
	public List<Long> retrieveReactivableOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status = 6 and user_action='SUSPENTATION' and bo.is_deleted='n' "
				+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { clientId, planId });
	}
	
	@Override
	public List<Long> retrieveReconnectingOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_order_price bop on bo.id =bop.order_id "
				+ " join b_contract_period dp on bo.contract_period = dp.id AND dp.contract_period ='Perpetual' "
				+ " join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status = 3 and bo.user_action='DISCONNECTION' and bo.is_deleted='n' "
				+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] {clientId, planId });
	}
	
	@Override
	public List<Long> retrieveChangingOrderSecondaryConnections(final Long clientId, final Long planId) {

		final String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status in (1,3) and bo.is_deleted='n' "
				+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { clientId, planId });
	}
	
	@Override
	public List<Long> retrieveRenewalOrderSecondaryConnections(final Long clientId, final Long planId,final Long orderStatus) {

		 String sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
				+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
				+ " and bc.is_delete='N' where bo.order_status = 1 and bo.is_deleted='n'  " 
				+ " and bo.user_action not in ('RENEWAL BEFORE AUTOEXIPIRY') "
				+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";
		 
		 if(orderStatus.equals(StatusTypeEnum.DISCONNECTED.getValue().longValue())) {
			 
		  sql = " SELECT bo.id as orderId from b_orders bo join b_plan_pricing bp on bp.plan_id=bo.plan_id and bp.is_deleted='n' "
					+ " join b_chargevariant bc on bc.id = bp.charging_variant and bc.chargevariant_code <> 'None' "
					+ " and bc.is_delete='N' where bo.order_status = 3 and bo.user_action='DISCONNECTION' and bo.is_deleted='n' " 
					/*+ " and bo.user_action not in ('RENEWAL BEFORE AUTOEXIPIRY','RENEWAL AFTER AUTOEXIPIRY') and bo.connection_type='SECONDARY'"*/
					+ " and bo.client_id= ? and bo.plan_id= ? group by bo.id ";
		 }

		return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { clientId, planId });
	}

	/* (non-Javadoc)
	 * @see #retrievCustomerRegionPlanPrices(Long, Long,String)
	 */
	@Override
	public List<PriceData> retrievCustomerRegionPlanPrices(final Long planId,final Long clientId, final String billFrequency,
			final String state, final String country) {
		
		final PlanPriceMapper mapper = new PlanPriceMapper();
		
		String sql = " SELECT da.id AS id,pd.id,if(da.service_code = 'None', 0, se.id) AS serviceId,da.service_code AS service_code, "
				+ "da.charge_code AS charge_code, da.charging_variant AS charging_variant,c.charge_type AS charge_type, "
				+ "c.charge_duration AS charge_duration,c.duration_type AS duration_type, da.discount_id AS discountId, "
				+ "c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,pd.state_id AS stateId, "
				+ "pd.country_id AS countryId, pd.state_id AS regionState,pd.country_id AS regionCountryId  "
				+ "FROM b_plan_pricing da LEFT JOIN b_charge_codes c ON c.charge_code = da.charge_code  "
				+ "LEFT JOIN b_service se ON (da.service_code = se.service_code OR da.service_code = 'None')  "
				+ "LEFT JOIN b_priceregion_detail pd ON pd.priceregion_id = da.price_region_id  AND pd.is_deleted='N' "
				+ "WHERE da.is_deleted = 'n' AND da.plan_id = ? AND c.billfrequency_code= ? "
				+ "AND pd.state_id in (0,(select id from b_state where state_name='" +state+ "' and is_delete='N')) "
				+ "AND pd.country_id = ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_country c "
				+ "where b.priceregion_id = a.price_region_id AND b.country_id = c.id AND c.country_name= '"+country+"'"
				+ "and c.is_active='Y' and a.is_deleted = 'n'),0) GROUP BY da.id ";

		if( clientId != null && clientId > 0) {
		
		     sql = "SELECT da.id AS id,if(da.service_code = 'None', 0, se.id) AS serviceId,da.service_code AS service_code,da.charge_code AS charge_code,"
				+ " da.charging_variant AS charging_variant,c.charge_type AS charge_type,c.charge_duration AS charge_duration,c.duration_type AS duration_type,"
				+ " da.discount_id AS discountId,c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,s.id AS stateId,s.parent_code AS countryId,"
				+ " pd.state_id AS regionState,con.country_name,pd.country_id AS regionCountryId"
				+ " FROM b_plan_pricing da LEFT JOIN b_charge_codes c ON c.charge_code = da.charge_code  "
				+ " LEFT JOIN b_service se ON (da.service_code = se.service_code OR da.service_code = 'None')"
				+ " LEFT JOIN b_priceregion_detail pd ON pd.priceregion_id = da.price_region_id"
				+ " JOIN b_client_address ca LEFT JOIN b_state s ON ca.state = s.state_name LEFT JOIN b_country con ON ca.country = con.country_name"
				+ " WHERE da.is_deleted = 'n' AND ca.address_key = 'PRIMARY' AND da.plan_id = ? AND (c.billfrequency_code = ?) "
				+ " AND ca.client_id ="+clientId+" AND (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_state c,"
				+ " b_charge_codes cc,b_client_address d"
				+ " WHERE b.priceregion_id = a.price_region_id  AND cc.charge_code = a.charge_code AND  b.state_id = c.id   AND a.price_region_id = b.priceregion_id "
				+ " AND d.state = c.state_name AND d.address_key = 'PRIMARY' AND d.client_id = "+clientId+" and a.plan_id= "+planId+" AND (cc.billfrequency_code = '"+billFrequency+"')),0)"
				+ " AND pd.country_id = ifnull( (SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_country c, b_charge_codes cc,b_client_address d "
				+ " WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND cc.charge_code = a.charge_code AND a.price_region_id = b.priceregion_id "
				+ " AND c.country_name = d.country AND d.address_key = 'PRIMARY' AND d.client_id = "+clientId+" AND a.plan_id = "+planId+" AND (cc.billfrequency_code ='"+billFrequency+"')),0))"
				+ " GROUP BY da.id";
		
		}
	
		
		return this.jdbcTemplate.query(sql, mapper, new Object[] { planId,billFrequency });
	}
	
	private static final class PlanPriceMapper implements RowMapper<PriceData> {

		@Override
		public PriceData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String serviceCode = rs.getString("service_code");
			final String chargeCode = rs.getString("charge_code");
			final String chargingVariant = rs.getString("charging_variant");
			final BigDecimal price = rs.getBigDecimal("price");
			final String chargeType = rs.getString("charge_type");
			final String chargeDuration = rs.getString("charge_duration");
			final String durationType = rs.getString("duration_type");
			final Long serviceid = rs.getLong("serviceId");
			final Long discountId = rs.getLong("discountId");
			final Long stateId = rs.getLong("stateId");
			final Long countryId = rs.getLong("countryId");
			final Long regionState = rs.getLong("regionState");
			final Long regionCountryId = rs.getLong("regionCountryId");
			boolean taxinclusive = rs.getBoolean("taxInclusive");

			return new PriceData(id, serviceCode, chargeCode, chargingVariant,price, chargeType, chargeDuration, durationType, serviceid,
					discountId, taxinclusive, stateId, countryId, regionState,regionCountryId);

		}

	  }
		
	@Override
	public List<ServiceData> retrieveReconnectionPrices(final Long clientId, final Long planId, final Long orderId) {

		OrderPriceMapper mapper = new OrderPriceMapper();
		final String sql = " select bo.id as id, bo.billing_frequency as billingFrequency," +
				" bo.contract_period as contractId, bop.price as price " +
				"from b_orders bo join b_order_price bop on bop.order_id = bo.id " +
				"where bo.client_id = ? and bo.plan_id = ? and bo.id = ? and order_status in (1,3) and bo.is_deleted='n' ";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, planId, orderId });

	}

	private static final class OrderPriceMapper implements RowMapper<ServiceData> {

		@Override
		public ServiceData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
			
			Long id = rs.getLong("id");
			String billingFrequency = rs.getString("billingFrequency");
			Long contractId = rs.getLong("contractId");
			BigDecimal price = rs.getBigDecimal("price");
			return new ServiceData(id, billingFrequency, contractId, price);
	}
	}
	
	@Override
	public List<EventMasterData> retrieveAllEvents(Long planId) {

		PlanEventMapper mapper = new PlanEventMapper();
		String sql = "select " + mapper.schema() + " and da.plan_id = '" + planId + "'";
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});

	}

	private static final class PlanEventMapper implements RowMapper<EventMasterData> {

		public String schema() {
			return " da.id as id,se.id as eventId, da.event_name as eventName, da.plan_id as planId from " +
					" b_plan_events da,b_mod_master se where da.event_name = se.event_name ";

		}

		@Override
		public EventMasterData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
			
			Long id = rs.getLong("id");
			Long eventId = rs.getLong("eventId");
			String eventName = rs.getString("eventName");
			Long planId = rs.getLong("planId");

			return new EventMasterData(id, eventId, eventName, planId);
	}
	}
	
	}



