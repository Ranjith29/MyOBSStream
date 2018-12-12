package org.obsplatform.portfolio.association.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;

import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.data.HardwareAssociationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HardwareAssociationReadplatformServiceImpl implements HardwareAssociationReadplatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public HardwareAssociationReadplatformServiceImpl(final ConfigurationRepository configurationRepository,
			final RoutingDataSource dataSource, final PlatformSecurityContext context)

	{
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}

	
	@Override
	public List<HardwareAssociationData> retrieveClientAllocatedPlan(Long clientId, String itemCode) {
		try {

			PlanMapper mapper = new PlanMapper();
			String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, itemCode });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class PlanMapper implements RowMapper<HardwareAssociationData> {

		public String schema() {

			return " o.id AS id, o.plan_id AS planId,hm.item_code as itemCode  FROM b_orders o,b_hw_plan_mapping hm, "
					+ " b_plan_master p  WHERE o.client_id =? AND hm.plan_code = p.plan_code "
					+ " AND o.plan_id = p.id AND hm.item_code=? "
					+ " AND o.id in (select id from b_orders os where os.client_id=o.client_id AND os.plan_id=p.id AND is_deleted='N' and o.order_status<>5 "
					+ " AND os.id NOT IN (SELECT a.order_id FROM b_association a WHERE o.id = a.order_id "
					+ " AND o.client_id = a.client_id AND a.is_deleted = 'N'));";

		}

		@Override
		public HardwareAssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			Long planId = rs.getLong("planId");
			Long orderId = rs.getLong("id");
			String itemCode = rs.getString("itemCode");

			return new HardwareAssociationData(id, null, planId, orderId, itemCode);

		}
	}

	@Override
	public List<AssociationData> retrieveClientAssociationDetails(Long clientId, String serialNo) {

		try {

			HarderwareAssociationMapper mapper = new HarderwareAssociationMapper();
			String sql = "select " + mapper.schema();
			if (serialNo != null) {
				sql = sql + " and a.hw_serial_no=" + "'" + serialNo + "'";
			}
			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class HarderwareAssociationMapper implements RowMapper<AssociationData> {

		public String schema() {
			return "a.id as id,a.order_id AS orderId,p.plan_code as planCode,i.item_code as itemCode, a.hw_serial_no AS serialNum "
					+ " FROM b_association a,b_plan_master p,b_allocation al,b_item_master i"
					+ " where a.plan_id=p.id and a.hw_serial_no=al.serial_no and al.item_master_id=i.id and a.client_id = ?";

		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			Long orderId = rs.getLong("orderId");
			String planCode = rs.getString("planCode");
			String itemCode = rs.getString("itemCode");
			String serialNum = rs.getString("serialNum");

			return new AssociationData(orderId, id, planCode, itemCode, serialNum, null);

		}
	}

	@Transactional
	@Override
	public List<AssociationData> retrieveHardwareData(Long clientId) {
		try {
			AssociationMapper mapper = new AssociationMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, clientId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class AssociationMapper implements RowMapper<AssociationData> {

		public String schema() {
			return " 'ALLOT' as allocationType,b.serial_no AS serialNum,b.provisioning_serialno as provisionNum, pdm.property_code as propertyCode, ba.order_id as orderId FROM  b_item_detail b  LEFT JOIN "
					+ "b_propertydevice_mapping pdm ON (b.serial_no = pdm.serial_number and b.client_id = pdm.client_id) Left JOIN b_association ba on (b.serial_no = ba.hw_serial_no) where  b.client_id=? "
					+ "and ba.is_deleted='N' union"
					+ " select  'OWNED' as allocationType,o.serial_number  AS serialNum, o.provisioning_serial_number  AS provisionNum, pdm.property_code as propertyCode, ba.order_id as orderId"
					+ " FROM b_owned_hardware o LEFT JOIN b_propertydevice_mapping pdm ON (o.serial_number = pdm.serial_number and o.client_id = pdm.client_id) Left JOIN b_association ba on (o.serial_number = ba.hw_serial_no)"
					+ " WHERE o.client_id = ? and o.is_deleted = 'N' and ba.is_deleted='N'";

		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final String serialNum = rs.getString("serialNum");
			final String provisionNumber = rs.getString("provisionNum");
			final String allocationType = rs.getString("allocationType");
			final String propertyCode = rs.getString("propertycode");
			final Long orderId = rs.getLong("orderId");
			return new AssociationData(serialNum, provisionNumber, allocationType, propertyCode, orderId);
		}
	}

	@Override
	 public List<AssociationData> retrieveCustomerHardwareAllocationData(final Long clientId, final Long orderId,final Long itemId) {

	   AllocationMapper mapper = new AllocationMapper();
	   StringBuilder  sql = new StringBuilder().append( "select ");
	   if (itemId != null && orderId != null) {
	    sql = sql.append(mapper.orderSchema()).append(" AND b.item_master_id=" + itemId + " AND oa.order_id="+ orderId);
	   } else if (itemId != null && orderId == null) {
	    sql = sql.append(mapper.schema()).append(" AND b.item_master_id=" + itemId);
	   } else {
	    sql = sql.append(mapper.allocationSchema());
	   }
	   return this.jdbcTemplate.query(sql.toString(), mapper, new Object[] { clientId, clientId });

	  
	 }

	 private static final class AllocationMapper implements RowMapper<AssociationData> {

	  public String allocationSchema() {
	   
	   return " 'ALLOT' as allocationType,b.serial_no AS serialNum,b.provisioning_serialno as provisionNum, b.item_master_id as itemId FROM  b_item_detail b"
	     + " where b.serial_no NOT IN (select hw_serial_no from b_association ba "
	     + " where b.serial_no = ba.hw_serial_no and b.client_id = ba.client_id and ba.is_deleted = 'N')"
	     + " and b.client_id=?  AND b.is_deleted ='N'" + " union"
	     + " select  'OWNED' as allocationType,o.serial_number  AS serialNum, o.provisioning_serial_number  AS provisionNum, o.item_type as itemId FROM b_owned_hardware o"
	     + " WHERE o.serial_number NOT IN (select hw_serial_no From b_association ba "
	     + " WHERE o.serial_number = ba.hw_serial_no and o.client_id = ba.client_id and ba.is_deleted = 'N') "
	     + " and o.client_id = ? AND o.is_deleted = 'N'";

	  }

	  public String schema() {
	   
	   return " 'ALLOT' AS allocationType,b.serial_no AS serialNum,b.provisioning_serialno AS provisionNum,b.item_master_id AS itemId "
	   		+ " FROM  b_item_detail b  JOIN b_allocation a ON b.serial_no = a.serial_no  AND b.is_deleted ='N' AND a.is_deleted ='N' "
	        + " WHERE b.client_id= ?  AND a.client_id = ?  AND  b.serial_no NOT IN (select hw_serial_no From b_association ba "
	        + " WHERE b.serial_no = ba.hw_serial_no and a.client_id = ba.client_id and ba.is_deleted = 'N') " ; 

	  }

	  public String orderSchema() {
	   
	   return " 'ALLOT' as allocationType,b.serial_no AS serialNum,b.provisioning_serialno as provisionNum,b.item_master_id as itemId"
	     + "  FROM  b_item_detail b INNER JOIN b_allocation a ON b.serial_no = a.serial_no "
	     + "  INNER JOIN b_association oa ON oa.client_id = b.client_id  AND a.serial_no = oa.hw_serial_no "
	     + "  AND b.client_id= ?  AND a.client_id = ? AND b.is_deleted ='N' AND a.is_deleted ='N' AND oa.is_deleted='N' ";

	  }

	  @Override
	  public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

	   final String serialNum = rs.getString("serialNum");
	   final String provisionNumber = rs.getString("provisionNum");
	   final String allocationType = rs.getString("allocationType");
	   final Long itemId = rs.getLong("itemId");
	   return new AssociationData(serialNum, provisionNumber, allocationType, null, itemId);
	  }
	 }

	@Override
	public List<AssociationData> retrieveplanData(Long clientId) {

		try {
			AssociationPlanMapper mapper = new AssociationPlanMapper();
			String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class AssociationPlanMapper implements RowMapper<AssociationData> {

		public String schema() {
			return "p.plan_code as planCode,p.id as id,o.id as orderId from b_orders o,b_plan_master p"
					+ " where o.plan_id=p.id and NOT EXISTS(Select * from  b_association a WHERE   a.order_id =o.id and a.is_deleted='N') and o.client_id=? ";
		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long planId = rs.getLong("id");
			String planCode = rs.getString("planCode");
			Long id = rs.getLong("orderId");
			return new AssociationData(planId, planCode, id);
		}
	}

	@Override
	public AssociationData retrieveSingleDetails(Long id) {

		try {
			Mapper mapper = new Mapper();
			String sql = " select *from (select a.id AS id,a.client_id AS clientId,a.order_id AS orderId,a.allocation_type as allocationType,"
					+ " i.id as itemId,a.hw_serial_no AS serialNo,p.plan_code AS planCode,id.serial_no AS serialNum,p.id AS planId,i.item_code AS itemCode,"
					+ " os.id as saleId "
					+ " FROM b_association a,b_plan_master p,b_item_detail id,b_item_master i, b_onetime_sale os WHERE p.id = a.plan_id "
					+ " AND a.order_id = ? AND id.serial_no = a.hw_serial_no AND id.item_master_id = i.id   AND a.is_deleted = 'N' and "
					+ " os.item_id =i.id and os.client_id = a.client_id group by id" + " union "
					+ " select a.id AS id,a.client_id AS clientId,a.order_id AS orderId,a.allocation_type as allocationType,i.id AS itemId,a.hw_serial_no AS serialNo,"
					+ " p.plan_code AS planCode,o.serial_number AS serialNum, p.id AS planId,i.item_code AS itemCode, null as saleId FROM b_association a,"
					+ " b_plan_master p,b_owned_hardware o,b_item_master i WHERE p.id = a.plan_id AND a.order_id =? AND o.serial_number = a.hw_serial_no "
					+ " AND o.item_type = i.id AND a.is_deleted = 'N' GROUP BY id) a limit 1";

			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { id, id });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class Mapper implements RowMapper<AssociationData> {

		/*
		 * public String schema() { return
		 * "  a.id AS id,a.client_id AS clientId,a.order_id AS orderId,a.allocation_type as allocationType,i.id as itemId,a.hw_serial_no AS serialNo,p.plan_code AS planCode,id.serial_no AS serialNum,"
		 * +
		 * " p.id AS planId,i.item_code AS itemCode,os.id as saleId FROM b_association a,b_plan_master p,b_item_detail id,b_item_master i, b_onetime_sale os"
		 * +
		 * "  WHERE p.id = a.plan_id AND a.order_id = ? AND id.serial_no = a.hw_serial_no AND id.item_master_id = i.id   AND a.is_deleted = 'N' and "
		 * + "  os.item_id =i.id and os.client_id = a.client_id group by id";
		 * 
		 * }
		 * 
		 * 
		 * public String ownDeviceSchema() { return
		 * "  a.id AS id,a.client_id AS clientId,a.order_id AS orderId,a.allocation_type as allocationType,i.id AS itemId,a.hw_serial_no AS serialNo,p.plan_code AS planCode,"
		 * +
		 * " o.serial_number AS serialNum, p.id AS planId,i.item_code AS itemCode, null as saleId FROM b_association a,b_plan_master p,b_owned_hardware o,"
		 * +
		 * " b_item_master i WHERE p.id = a.plan_id AND a.order_id =? AND o.serial_number = a.hw_serial_no AND o.item_type = i.id "
		 * + " AND a.is_deleted = 'N' GROUP BY id";
		 * 
		 * }
		 */

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long clientId = rs.getLong("clientId");
			final Long orderId = rs.getLong("orderId");
			final String planCode = rs.getString("planCode");
			final String itemCode = rs.getString("itemCode");
			final String provNum = rs.getString("serialNo");
			final String serialNum = rs.getString("serialNum");
			final Long planId = rs.getLong("planId");
			final Long saleId = rs.getLong("saleId");
			final Long itemId = rs.getLong("itemId");
			final String allocationType = rs.getString("allocationType");
			
			return new AssociationData(orderId, planCode, provNum, id, planId, clientId, serialNum, 
					itemCode, saleId,itemId, allocationType);

		}

	}

	@Transactional
	@Override
	public List<HardwareAssociationData> retrieveClientAllocatedHardwareDetails(Long clientId) {

		try {
			final String sql = " SELECT *FROM (SELECT a.id AS id,a.serial_no AS serialNo,a.provisioning_serialno AS provSerialNum"
					+ " FROM b_item_detail a, b_allocation l WHERE     a.serial_no = l.serial_no AND l.client_id = ? AND l.is_deleted = 'N'"
					+ " AND a.client_id IS NULL" + " UNION "
					+ " SELECT o.id AS id, o.serial_number AS serialNo, o.provisioning_serial_number AS provSerialNum "
					+ " FROM b_owned_hardware o WHERE o.client_id = ? AND o.is_deleted = 'N' AND o.id = (SELECT max(id)"
					+ " FROM b_owned_hardware a WHERE a.client_id = o.client_id)) a";
			
			ClientHarderwareMapper mapper = new ClientHarderwareMapper();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, clientId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class ClientHarderwareMapper implements RowMapper<HardwareAssociationData> {
		/*
		 * public String schema() { return
		 * " max(a.id) AS id,a.serial_no AS serialNo,a.provisioning_serialno AS provSerialNum  "
		 * +
		 * " FROM b_item_detail a, b_allocation l where a.serial_no = l.serial_no and l.client_id = ? "
		 * + " and l.is_deleted = 'Y' and a.client_id is null";
		 * 
		 * }
		 * 
		 * public String ownDeviceSchema() { return
		 * " o.id as id ,o.serial_number as serialNo,o.provisioning_serial_number as provSerialNum   FROM b_owned_hardware o"
		 * +
		 * " where o.client_id = ? and o.is_deleted='Y' and o.id=(select max(id) from b_owned_hardware a where a.client_id= o.client_id )"
		 * ;
		 * 
		 * }
		 */

		@Override
		public HardwareAssociationData mapRow(ResultSet rs, int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			String serialNo = rs.getString("serialNo");
			String provSerialNum = rs.getString("provSerialNum");

			return new HardwareAssociationData(id, serialNo, provSerialNum);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveClientAllocatedPlanByServiceMap(java.lang.Long,
	 * java.lang.Long)
	 */
	@Override
	public List<AllocationDetailsData> retrieveClientAllocatedPlanByServiceMap(Long clientId, Long itemId) {

		try {

			final serviceLevelMapper mapper = new serviceLevelMapper();
			final String sql = "SELECT " + mapper.schema();
			return jdbcTemplate.query(sql, mapper, new Object[] { clientId, itemId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class serviceLevelMapper implements RowMapper<AllocationDetailsData> {

		public String schema() {

			return " o.id as orderId, psd.service_id as serviceId,'ALLOT' as allocationType,id.serial_no AS serialNo,i.item_description AS itemDescription,"
					+ " o.plan_id AS planId FROM b_item_master i,b_item_detail id,b_prov_service_details psd,b_orders o,b_order_line ol"
					+ " WHERE NOT EXISTS( SELECT * FROM  b_association a WHERE a.order_id = o.id AND a.client_id = o.client_id "
					+ " AND psd.service_id = a.service_id AND a.is_deleted = 'N') "
					+ " AND i.id = id.item_master_id AND psd.item_id = id.item_master_id AND psd.is_hw_req = 'Y' AND o.id=ol.order_id "
					+ " AND ol.service_id = psd.service_id AND id.is_deleted = 'N' AND o.id=(select max(id) from b_orders where client_id=id.client_id and client_id=o.client_id) "
					+ " And  id.client_id = ? AND psd.item_id= ? group by serviceId ";
		}

		@Override
		public AllocationDetailsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long serviceId = rs.getLong("serviceId");
			final Long orderId = rs.getLong("orderId");
			final String allocationType = rs.getString("allocationType");
			final String serialNum = rs.getString("serialNo");
			final String itemDescription = rs.getString("itemDescription");
			final Long planId = rs.getLong("planId");

			return new AllocationDetailsData(serviceId, planId, orderId, allocationType, serialNum, itemDescription);
		}
	}

	@Override
	public AssociationData retrieveAssociationsDetailsWithSerialNum(Long clientId, String serialNumber) {

		this.context.authenticatedUser();
		AssociationDataMapper mapper = new AssociationDataMapper();
		String sql = "select " + mapper.schema() + " where os.client_id = ? and bi.serial_no = ? ";

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { clientId, serialNumber });
	}

	private static final class AssociationDataMapper implements RowMapper<AssociationData> {

		public String schema() {

			return " os.id AS saleId,os.office_id AS officeId,bi.serial_no as serialNumber, "
					+ " bi.item_master_id as itemMasterId,bi.warranty_date as warrantyDate "
					+ " from b_item_detail bi JOIN b_onetime_sale os ON os.item_id=bi.item_master_id AND os.client_id = bi.client_id "
					+ " AND os.is_deleted='N' JOIN b_allocation ba ON ba.serial_no=bi.serial_no AND ba.order_id=os.id AND "
					+ " ba.client_id=os.client_id AND ba.is_deleted='N' ";
		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long saleId = rs.getLong("saleId");
			final Long officeId = rs.getLong("officeId");
			final String serialNum = rs.getString("serialNumber");
			final Long itemMasterId = rs.getLong("itemMasterId");
			final LocalDate warrantyDate = JdbcSupport.getLocalDate(rs,"warrantyDate");

			return new AssociationData(officeId, serialNum, saleId,itemMasterId, warrantyDate);
		}

	}

	@Override
	public List<AssociationData> retrieveClientAssociationDetailsForProperty(Long clientId, String serialNumber) {

		try {

			HarderwareAssociation mapper = new HarderwareAssociation();
			String sql = "select " + mapper.schema();
			sql = sql + " and a.hw_serial_no= ?";

			return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId, serialNumber });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class HarderwareAssociation implements RowMapper<AssociationData> {

		public String schema() {
			return "a.id as id,a.order_id AS orderId, a.hw_serial_no AS serialNum FROM b_association a where a.client_id = ?";

		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			Long orderId = rs.getLong("orderId");
			String serialNum = rs.getString("serialNum");

			return new AssociationData(orderId, id, null, null, serialNum, null);

		}

	}

	@Override
	public String retrieveClientTalkSerialNo(Long clientId) {
		String sql = "select hw_serial_no as serialno from b_association where client_id=? and plan_id=4 and is_deleted='N';";
		return this.jdbcTemplate.queryForObject(sql, String.class,new Object[]{clientId});
	}


	@Override
	public String retrieveClientTalkgoSerialNo(Long clientId, Long orderId, Long planId) {
		String sql = "select hw_serial_no as serialno from b_association where client_id=? and order_id=? and plan_id=? and is_deleted='N';";
		return this.jdbcTemplate.queryForObject(sql, String.class,new Object[]{clientId,orderId,planId});
	}
	
	
	@Override
	public String retrieveClientTalkSerialNoFirstNo(Long clientId) {
		String sql = "select hw_serial_no as serialno from b_association where " +
				" client_id=?  and plan_id=13 and is_deleted='N' group by client_id ORDER BY id  ASC limit 1";
		return this.jdbcTemplate.queryForObject(sql, String.class,new Object[]{clientId});
	}
	
}
