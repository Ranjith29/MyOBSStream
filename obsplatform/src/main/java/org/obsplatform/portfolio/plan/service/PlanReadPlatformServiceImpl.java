package org.obsplatform.portfolio.plan.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.planprice.service.PriceReadPlatformService;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.partner.data.PartnersData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.order.data.VolumeTypeEnumaration;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.plan.domain.VolumeTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


/**
 * @author hugo
 *
 */
@Service
public class PlanReadPlatformServiceImpl implements PlanReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private  final  PriceReadPlatformService priceReadPlatformService;
	public final static String POST_PAID="postpaid";
	public final static String PREPAID="prepaid";
	
	

	@Autowired
	public PlanReadPlatformServiceImpl(final PlatformSecurityContext context,final PriceReadPlatformService priceReadPlatformService,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.priceReadPlatformService=priceReadPlatformService;
	}

	@Override
	public List<PlanData> retrievePlanData(final String planType) {

	context.authenticatedUser();
	String sql=null;
	PlanDataMapper mapper = new PlanDataMapper(this.priceReadPlatformService);
	
		if(planType!=null && PREPAID.equalsIgnoreCase(planType)){
			sql = "select " + mapper.schema()+" AND pm.is_prepaid ='Y' "+"group by pm.id";
		}else if(planType!=null && planType.equalsIgnoreCase(POST_PAID)){
		sql = "select " + mapper.schema()+" AND pm.is_prepaid ='N' "+"group by pm.id";
		}else{
			sql = "select " + mapper.schema()+ "group by pm.id";
		}
   
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class PlanDataMapper implements RowMapper<PlanData> {
         private final PriceReadPlatformService priceReadPlatformService;
		
         public PlanDataMapper(final PriceReadPlatformService priceReadPlatformService) {
			this.priceReadPlatformService=priceReadPlatformService;
		}

		public String schemaForpartner(Long userId) {
			
			return " SELECT pm.id,pm.plan_code AS planCode,pm.plan_description AS planDescription,pm.start_date AS startDate," +
					" pm.end_date AS endDate,pm.plan_status AS planStatus, pm.is_prepaid AS isprepaid, pm.provision_sys AS provisionSystem" +
					" FROM b_plan_master pm left outer join b_plan_qualifier pq on pm.id =pq.plan_id left outer join m_office mo on mo.id=pq.partner_id" +
					" WHERE pm.is_deleted = 'n' and mo.hierarchy like '%' group by pm.id";
		}

		public String schema() {
			return  " pm.id,pm.plan_code as planCode,pm.plan_description as planDescription,pm.start_date as startDate,"+
                    " pm.end_date as endDate,pm.plan_status as planStatus,pm.is_prepaid AS isprepaid,"+
                    " pm.provision_sys as provisionSystem,count(o.id) as orders  FROM  b_plan_master pm"+ 
                    " LEFT OUTER JOIN"+ 
                    " b_orders o on (pm.id = o.plan_id and o.order_status in(1,4))"+
                    " WHERE pm.is_deleted = 'n' ";

		}

		@Override
		public PlanData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String planCode = rs.getString("planCode");
			final String planDescription = rs.getString("planDescription");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
		    final Long planStatus = rs.getLong("planStatus");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
			final EnumOptionData enumstatus=OrderStatusEnumaration.OrderStatusType(planStatus.intValue());
			List<ServiceData> services=null;
			if(rs.getString("isprepaid").equalsIgnoreCase("Y")){
			services=priceReadPlatformService.retrieveServiceDetails(id);
			}
			final Long count=rs.getLong("orders");
			final String provisionSystem=rs.getString("provisionSystem");
			return new PlanData(id, planCode, startDate, endDate,null,null, planStatus, planDescription, provisionSystem, enumstatus,
					null,null, null,null,null,services,null,null,count,null,null);
		}
	}

	@Override
	public List<SubscriptionData> retrieveSubscriptionData(final Long orderId,final String planType) {

		context.authenticatedUser();
		SubscriptionDataMapper mapper = new SubscriptionDataMapper();
		String sql =null;
		if(planType != null && orderId != null && PREPAID.equalsIgnoreCase(planType)){
			
			 sql = "select " + mapper.schemaForPrepaidPlans()+" and o.id="+orderId+" GROUP BY sb.contract_period order by sb.contract_period";
		}else{
		    sql = "select " + mapper.schema()+" group by sb.contract_period order by contract_period";
		}
		
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}


	private static final class SubscriptionDataMapper implements RowMapper<SubscriptionData> {

		public String schema() {
			return " sb.id as id,sb.contract_period as contractPeriod,sb.contract_duration as units,sb.contract_type as contractType,0 as priceId "
					+ " from b_contract_period sb where is_deleted='N'";

		}
		
		public String schemaForPrepaidPlans() {
			
			
		return " sb.id AS id,sb.contract_period AS contractPeriod,sb.contract_duration AS units,sb.contract_type AS contractType," +
			   " p.id as  priceId,prm.priceregion_code as priceRegionCode, p.plan_id as planId,p.service_code as serviceCode" +
			   " FROM b_contract_period sb,b_orders o LEFT JOIN b_client_address ca ON ca.client_id = o.client_id LEFT JOIN b_state s" +
			   " ON s.state_name = ca.state LEFT JOIN b_priceregion_detail pd " +
			   " on (pd.state_id = ifnull((SELECT DISTINCT c.id FROM  b_plan_pricing a, b_priceregion_detail b, b_state c," +
			   " b_client_address d WHERE b.priceregion_id = a.price_region_id AND b.state_id = c.id AND d.state = c.state_name AND " +
			   " d.address_key = 'PRIMARY' AND d.client_id =o.client_id  and a.plan_id = o.plan_id),0) and pd.country_id = ifnull((SELECT DISTINCT c.id" +
			   " FROM b_plan_pricing a,b_priceregion_detail b,b_country c, b_state s,b_client_address d" +
			   " WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND c.country_name = d.country AND d.address_key = 'PRIMARY'" +
			   " AND d.client_id =o.client_id and a.plan_id = o.plan_id and  d.state = s.state_name and " +
			   " (s.id =b.state_id or(b.state_id = 0 and b.country_id = c.id ))), 0)) LEFT JOIN b_priceregion_master prm ON prm.id = pd.priceregion_id" +
			   " JOIN b_plan_pricing p ON p.plan_id = o.plan_id AND p.is_deleted='N' AND p.price_region_id = prm.id WHERE     sb.is_deleted = 'N' AND sb.contract_period = p.duration" +
			   " AND o.plan_id = p.plan_id ";	

			
		}

		@Override
		public SubscriptionData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String contractPeriod = rs.getString("contractPeriod");
			final String subscriptionType = rs.getString("contractType");
			final Long priceId = rs.getLong("priceId");
			return new SubscriptionData(id,contractPeriod,subscriptionType,priceId);
		}

	}

	/*
	 *Method for Status Retrieval
	 */
	@Override
	public List<EnumOptionData> retrieveNewStatus() {
		
		final EnumOptionData active = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE);
		final EnumOptionData inactive = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.INACTIVE);
		return 	Arrays.asList(active, inactive);
			
	}

	@Override
	public PlanData retrievePlanData(final Long planId) {
		   
		    context.authenticatedUser();
	        final String sql = "SELECT pm.id AS id,pm.plan_code AS planCode,pm.plan_description AS planDescription,pm.start_date AS startDate,pm.end_date AS endDate,"
	        		   +"pm.plan_status AS planStatus,pm.provision_sys AS provisionSys,pm.bill_rule AS billRule,pm.is_prepaid as isPrepaid,"
	        		  +" pm.allow_topup as allowTopup,pm.plan_Notes as planNotes,pm.trial_days as trialPeriodDays,v.volume_type as volumeType, v.units as units,pm.is_hw_req as isHwReq,v.units_type as unitType,count(o.id) as orders"+
	        		   " FROM (b_plan_master pm  left join b_volume_details v on pm.id = v.plan_id) LEFT OUTER JOIN b_orders o on (pm.id = o.plan_id and o.order_status in(1,4))" +
	        		  "  WHERE pm.id = ? AND pm.is_deleted = 'n' group by pm.id";


	        RowMapper<PlanData> rm = new ServiceMapper();

	        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { planId });
	
		}


	 private static final class ServiceMapper implements RowMapper<PlanData> {

	        @Override
	        public PlanData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

	            final Long id = rs.getLong("id");
	            final String planCode = rs.getString("planCode");
	            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
	            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
	            final Long billRule = rs.getLong("billRule");
	            final Long planStatus = rs.getLong("planStatus");
	            final String planDescription = rs.getString("planDescription");
	            final String provisionSys=rs.getString("provisionSys");
	            final String isPrepaid=rs.getString("isPrepaid");
	            final String volume=rs.getString("volumeType");
	            final String allowTopup=rs.getString("allowTopup");
	            final String isHwReq=rs.getString("isHwReq");
	            final String units=rs.getString("units");
	            final String unitType=rs.getString("unitType");
	            final Long count=rs.getLong("orders");
	            final String planNotes=rs.getString("planNotes");
	            final Long trialPeriodDays=rs.getLong("trialPeriodDays");
	            
	            return new PlanData(id,planCode,startDate,endDate,billRule,null,planStatus,planDescription,
	            		provisionSys,null,isPrepaid,allowTopup,volume,units,unitType,null,null,isHwReq,count,planNotes,trialPeriodDays);
	        }
	}



	/* @param planId
	 * @return PlanDetails
	 */
	@Override
	public List<ServiceData> retrieveSelectedServices(final Long planId) {

		context.authenticatedUser();

		String sql = "SELECT sm.id AS id,sm.service_description AS serviceDescription,p.plan_code AS planCode,"
				+ "pm.service_code AS serviceCode,psd.image AS image "
				+ "FROM b_plan_detail pm,b_plan_master p,b_service sm "
				+ "left join b_prov_service_details psd on psd.service_id = sm.id "
				+ "WHERE pm.service_code = sm.service_code AND p.id = pm.plan_id AND sm.is_deleted = 'N'"
				+ " AND pm.plan_id = ? GROUP BY sm.id;";

		RowMapper<ServiceData> rm = new PeriodMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] { planId });
	}

	private static final class PeriodMapper implements RowMapper<ServiceData> {

		@Override
		public ServiceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceDescription = rs.getString("serviceDescription");
			final String image = rs.getString("image");
			return new ServiceData(id, serviceCode, serviceDescription, image);

		}
	}

	@Override
	public List<EnumOptionData> retrieveVolumeTypes() {

		final EnumOptionData iptv = VolumeTypeEnumaration.VolumeTypeEnum(VolumeTypeEnum.IPTV);
		final EnumOptionData vod = VolumeTypeEnumaration.VolumeTypeEnum(VolumeTypeEnum.VOD);
		return Arrays.asList(iptv, vod);

	}

	@Override
	public List<PartnersData> retrieveAvailablePartnersData(Long planId) {

		try {
			this.context.authenticatedUser();
			PlanQulifierMapper mapper = new PlanQulifierMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { planId });

		} catch (EmptyResultDataAccessException dve) {
			return null;
		}

	}

	private static final class PlanQulifierMapper implements RowMapper<PartnersData> {

		public String schema() {
			return " o.id as id, o.name as partnerName"
					+ " FROM m_office o, m_code_value cv"
					+ " WHERE  o.id  NOT IN (select partner_id from b_plan_qualifier where plan_id = ? ) "
					+ " and cv.id = o.office_type AND cv.code_value ='Agent'";

		}

		public String schemaForPartners() {

			return "o.id AS id, o.name AS partnerName"
					+ " FROM m_office o, m_code_value cv, b_plan_qualifier pq "
					+ "WHERE cv.id = o.office_type AND cv.code_value = 'Agent' AND o.id =pq.partner_id  and pq.plan_id=?";
		}

		@Override
		public PartnersData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String partnerName = rs.getString("partnerName");
			return new PartnersData(id, partnerName, null);
		}

	}

	@Override
	public List<PartnersData> retrievePartnersData(Long planId) {

		try {
			this.context.authenticatedUser();
			PlanQulifierMapper mapper = new PlanQulifierMapper();
			final String sql = "select " + mapper.schemaForPartners();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { planId });

		} catch (EmptyResultDataAccessException dve) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveAllPlanDetails()
	 */
	@Override
	public List<PlanData> retrieveAllPlanDetails() {
		
		try{
			final PlanDetailsMapper mapper=new PlanDetailsMapper();  
			final String sql="select "+mapper.schema() + " where p.is_deleted='N'"; 
			return this.jdbcTemplate.query(sql,mapper,new Object[] {});		
			
		}catch(EmptyResultDataAccessException dve){
			return null;
		}
		
	}

	private static final class PlanDetailsMapper implements RowMapper<PlanData> {

		public String schema() {

			return " p.id as planId, p.plan_code as planName,p.plan_description as planDescription, p.start_date as startDate,p.plan_status as status,"
					+ " p.is_prepaid as isPrepaid,provision_sys as provisionSystem from b_plan_master p ";
		}

		@Override
		public PlanData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long planId = rs.getLong("planId");
			final String planName = rs.getString("planName");
			final String planDescription = rs.getString("planDescription");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
			final Long status = rs.getLong("status");
			final String isPrepaid = rs.getString("isPrepaid");
			final String provisionSystem = rs.getString("provisionSystem");
			
			return new PlanData(planId,planName,planDescription,startDate,status,isPrepaid,provisionSystem);
		}

	}
	
	
	
	@Override
	public List<EventMasterData> retrieveSelectedEvents(final Long planId) {

		context.authenticatedUser();

		/** old one 
		 * String sql = "select bmm.id as id, bmm.event_name as eventName, bmm.event_description as eventDescription"+
						" from b_plan_events bpe, b_plan_master bpm, b_mod_master bmm where bmm.event_name = bpe.event_name"+
						" and bpe.plan_id = bpm.id and bmm.status = 1 and bpe.plan_id = ? ";*/
		
		String sql ="select  m.id as id, m.event_name as eventName, m.event_description as eventDescription " +
				  " from b_plan_events pe join b_mod_master m on m.id=pe.event_name and m.status = 1 " +
				  " join b_plan_master pm on pm.id=pe.plan_id " +
				  " where pe.is_deleted='N' and pe.plan_id= ? ";

		RowMapper<EventMasterData> rm = new PlanEventMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] { planId });
	}

	private static final class PlanEventMapper implements RowMapper<EventMasterData> {

		@Override
		public EventMasterData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String eventName = rs.getString("eventName");
			final String eventDescription = rs.getString("eventDescription");
			return new EventMasterData(id, eventName, eventDescription);

		}
	}
	
	@Override
	public List<PlanData> findTalkPlanAddons(final Long planId) {

		try{
			final PlanTalkAddonMapper mapper=new PlanTalkAddonMapper();  
			final String sql="select "+mapper.schema() + " where ads.plan_id = ? and s.is_deleted = 'N'"; 
			return this.jdbcTemplate.query(sql,mapper,new Object[] { planId });		
			
		}catch(EmptyResultDataAccessException dve){
			return null;
		}
	}

	private static final class PlanTalkAddonMapper implements RowMapper<PlanData> {

		public String schema() {

			return " pd.plan_id as planId, s.service_code as planName,se.id as serviceId,se.service_code as serviceName, " +
					" cc.id as chargeCodeId, adp.price as price" +
					" from b_service s join b_plan_detail pd on (s.service_code = pd.service_code and pd.is_deleted = 'N') " +
					" join b_addons_service ads on (ads.plan_id = pd.plan_id and ads.is_deleted = 'N') " +
					" join b_addons_service_price adp on (adp.adservice_id = ads.id and adp.is_deleted = 'N') " +
					" join b_service se on (se.id = adp.service_id and se.is_deleted = 'N') " +
					" join b_charge_codes cc on (cc.charge_code = ads.charge_code) ";
		}

		@Override
		public PlanData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long planId = rs.getLong("planId");
			final String planName = rs.getString("planName");
			final Long serviceId = rs.getLong("serviceId");
			final String serviceName = rs.getString("serviceName");
			final Long chargeCodeId = rs.getLong("chargeCodeId");
			final BigDecimal price = rs.getBigDecimal("price");
			
			return new PlanData(planId, planName, serviceId, serviceName, chargeCodeId, price);
		}

	}
	
	public List<PlanData> retrieveSelectedClientCategorys(Long planId) {
		try {
			final PlanSelectedCategoryMapper mapper = new PlanSelectedCategoryMapper();

			final String sql = "select " + mapper.clientCategorySchema() + " where bpc.is_deleted = 'N' and bpc.plan_id = ? ";

			return jdbcTemplate.query(sql, mapper, new Object[] { planId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class PlanSelectedCategoryMapper implements RowMapper<PlanData> {

        public String clientCategorySchema() {
            return " bpc.client_category_id as id, bpc.plan_id as planId, bpc.client_category_id as clientCategoryId, " +
            		" mcv.code_value as clientCategory from b_plan_category_detail bpc " +
            		" join m_code_value mcv ON mcv.id = bpc.client_category_id ";
        }

        @Override
        public PlanData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long planId = rs.getLong("planId");
            final Long clientCategoryId = rs.getLong("clientCategoryId");
            final String clientCategory = rs.getString("clientCategory");
            
            return new PlanData(id, planId, clientCategoryId, clientCategory);
            
        }
    }
	
	@Override
	public List<PlanData> retrieveClientCategories() {
		try {
			final ClientCategorysMapper mapper = new ClientCategorysMapper();

			final String sql = "select " + mapper.clientCategorySchema() ;

			return jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class ClientCategorysMapper implements RowMapper<PlanData> {

        public String clientCategorySchema() {
            return " mcv.id AS id, mcv.code_value AS clientCategory FROM m_code_value mcv, " +
            		" m_code mc where mcv.code_id = mc.id and mc.code_name = 'Client Category' order by order_position asc ";
        }

        @Override
        public PlanData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String clientCategory = rs.getString("clientCategory");
            
            return new PlanData(id, clientCategory);
            
        }
    }
}
