package org.obsplatform.portfolio.allocation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class AllocationReadPlatformServiceImpl implements AllocationReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AllocationReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		
		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	@Override
	public List<AllocationDetailsData> getTheHardwareItemDetails(final Long orderId,final String serialNumber) {
		try {
			final ClientOrderMapper mapper = new ClientOrderMapper();
			 String sql =null;
			if(serialNumber!=null){
				  sql = " Select * from ( select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,id.provisioning_serialno AS serialNum,id.serial_no AS serialno,a.client_id AS clientId" +
					  		" FROM b_association a, b_item_detail id WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='N' and a.hw_serial_no=? " +
					  		" and allocation_type='ALLOT'" +
					  		" union all" +
					  		" select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,o.provisioning_serial_number AS serialNum,o.serial_number AS serialno,a.client_id AS clientId" +
					  		" FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no and  a.hw_serial_no=? " +
					  		" AND a.is_deleted = 'N' and o.is_deleted = 'N' and allocation_type='OWNED') a ";
					  		
					  
				return jdbcTemplate.query(sql, mapper, new Object[] {  orderId,serialNumber,orderId,serialNumber});
				
			}else{
				  sql = " Select * from ( select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,id.provisioning_serialno AS serialNum,id.serial_no AS serialno,a.client_id AS clientId" +
				  		" FROM b_association a, b_item_detail id WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='N'" +
				  		" and allocation_type='ALLOT'" +
				  		" union all" +
				  		" select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,o.provisioning_serial_number AS serialNum,o.serial_number AS serialno,a.client_id AS clientId" +
				  		" FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no " +
				  		" AND a.is_deleted = 'N' and o.is_deleted = 'N' and allocation_type='OWNED') a ";
				  		
				  
			return jdbcTemplate.query(sql, mapper, new Object[] {  orderId,orderId });
			}
			} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOrderMapper implements RowMapper<AllocationDetailsData> {

		/*
		 * public String clientAssociationLookupSchema() { 
		 * return " a.id AS id,a.order_id AS orderId,id.provisioning_serialno AS serialNum,a.client_id AS clientId FROM b_association a, b_item_detail id"
		 * + " WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='N' limit 1" ; }
		 * 
		 * public String clientOwnHwAssociationLookupSchema() {
		 * 
		 * return "  a.id AS id,a.order_id AS orderId,o.provisioning_serial_number AS serialNum,a.client_id AS clientId"
		 * + " FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no   "
		 * + " AND a.is_deleted = 'N' and o.is_deleted = 'N' limit 1"; }
		 */

		/*
		 * public String clientDeAssociationLookupSchema() { 
		 * return " a.id AS id, a.order_id AS orderId,a.client_id AS clientId,i.provisioning_serialno as serialNum FROM b_association a, b_item_detail i  "
		 * + " WHERE order_id = ? and a.hw_serial_no=i.serial_no AND a.id = (SELECT MAX(id) FROM b_association a WHERE  a.client_id =?  and a.is_deleted = 'Y')  limit 1"; }
		 * 
		 * public String clientOwnHwDeAssociationLookupSchema() { 
		 * return " a.id AS id,a.order_id AS orderId,a.client_id AS clientId,a.hw_serial_no AS serialNum FROM b_association a  "
		 * + " WHERE order_id =? AND a.id = (SELECT MAX(id) FROM b_association a  WHERE a.client_id =? AND a.is_deleted = 'Y') LIMIT 1" ; }
		 */

		@Override
		public AllocationDetailsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

	    final Long id = rs.getLong("id");	
		final Long serviceId = rs.getLong("serviceId");
		final Long orderId = rs.getLong("orderId");
		final Long planId = rs.getLong("planId");
		final String serialNum = rs.getString("serialNum");
		final String serialno = rs.getString("serialno");
		final Long clientId = rs.getLong("clientId");
		
		return new AllocationDetailsData(id,serviceId,planId,orderId,serialNum,clientId,serialno);
		  }
	}

	@Override
	public List<AllocationDetailsData> retrieveHardWareDetailsByItemCode(final Long clientId, final String planCode) {
		try {

			final HardwareMapper mapper = new HardwareMapper();
			final String sql = "select " + mapper.schema();
			return jdbcTemplate.query(sql, mapper, new Object[] { clientId,
					planCode, clientId, planCode });

		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class HardwareMapper implements RowMapper<AllocationDetailsData> {

		public String schema() {

			return " 'OWNED' as allocationType,o.id AS id,o.serial_number AS serialNo,i.item_description AS itemDescription  FROM b_item_master i,"
					+ " b_owned_hardware o, b_hw_plan_mapping hm WHERE o.serial_number NOT IN ( select hw_serial_no From b_association ba WHERE o.serial_number = ba.hw_serial_no and o.client_id = ba.client_id and ba.is_deleted ='N')"
					+ " AND o.item_type = i.id AND i.item_code = hm.item_code AND o.client_id =?  "
					+ " AND hm.plan_code =? and o.is_deleted = 'N' "
					+ " union "
					+ " select 'ALLOT' as allocationType,id.id AS id,id.serial_no AS serialNo,i.item_description AS itemDescription  "
					+ " FROM b_item_master i, b_item_detail id, b_hw_plan_mapping hm  WHERE id.serial_no NOT IN ( select hw_serial_no From b_association ba WHERE id.serial_no = ba.hw_serial_no and id.client_id = ba.client_id and ba.is_deleted ='N')"
					+ " AND id.item_master_id = i.id AND i.item_code =hm.item_code "
					+ " AND id.client_id =? and hm.plan_code=? ";
		}

		@Override
		public AllocationDetailsData mapRow(final ResultSet rs, final int rowNum)	throws SQLException {

			final Long id = rs.getLong("id");
			final String serialNum = rs.getString("serialNo");
			final String itemDescription = rs.getString("itemDescription");
			final String allocationType = rs.getString("allocationType");

			return new AllocationDetailsData(id, itemDescription, serialNum,null, null, allocationType, null, null, null);
		}
	}

	@Override
	public List<String> retrieveHardWareDetails(final Long clientId) {
		try {

			final ClientHardwareMapper mapper = new ClientHardwareMapper();
			final String sql = "select " + mapper.schema();
			return jdbcTemplate.query(sql, mapper, new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientHardwareMapper implements RowMapper<String> {

		public String schema() {
			return " a.serial_no as serialNo from b_allocation a where a.client_id=?";
		}

		@Override
		public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final String serialNum = rs.getString("serialNo");

			return serialNum;
		}
	}

	@Override
	public AllocationDetailsData getDisconnectedHardwareItemDetails(final Long orderId, final Long clientId) {

		try {

			final ClientOrderMapper mapper = new ClientOrderMapper();

			final String sql = "select *from (SELECT a.id AS id,a.order_id AS orderId,a.client_id AS clientId,"
					+ " i.provisioning_serialno AS serialNum FROM b_association a, b_item_detail i WHERE  order_id = ?  AND a.hw_serial_no = i.serial_no"
					+ " AND a.id = (SELECT MAX(id) FROM b_association a WHERE a.client_id = ? AND a.is_deleted = 'Y')"
					+ " UNION "
					+ " SELECT a.id AS id,a.order_id AS orderId,a.client_id AS clientId,a.hw_serial_no AS serialNum FROM b_association a"
					+ " WHERE order_id = ? AND a.id = (SELECT MAX(id) FROM b_association a WHERE a.client_id = ? AND a.is_deleted = 'Y')) "
					+ "a limit 1";

			/*
			 * if(associationType.equalsIgnoreCase(ConfigurationConstants. CONFIR_PROPERTY_SALE)){
			 * 
			 * sql = "select " + mapper.clientDeAssociationLookupSchema();
			 * 
			 * }else if(associationType.equalsIgnoreCase(ConfigurationConstants. CONFIR_PROPERTY_OWN)){
			 * 
			 * sql = "select " + mapper.clientOwnHwDeAssociationLookupSchema();
			 * }
			 */

			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {orderId, clientId, orderId, clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveHardWareDetailsByServiceMap(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<AllocationDetailsData> retrieveHardWareDetailsByServiceMap(final Long clientId, final Long serviceId) {

		try {

			final HardwareServiceMapper mapper = new HardwareServiceMapper();
			final String sql = "select " + mapper.schema();
			return jdbcTemplate.query(sql, mapper, new Object[] {serviceId, clientId,serviceId });

		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class HardwareServiceMapper implements	RowMapper<AllocationDetailsData> {

		public String schema() {

			return " id.id AS id,'ALLOT' as allocationType,id.serial_no AS serialNo,i.item_description AS itemDescription "
					+ " FROM b_item_master i INNER JOIN b_item_detail id ON i.id=id.item_master_id AND id.is_deleted='N' "
					+ " INNER JOIN b_prov_service_details psd ON psd.item_id=id.item_master_id AND psd.is_hw_req='Y' "
					+ " WHERE  id.client_id NOT IN ( select client_id From b_association ba WHERE  id.client_id = ba.client_id "
					+ " AND ba.service_id = ? and ba.is_deleted ='N') AND id.client_id =? and psd.service_id=? ";
		}

		@Override
		public AllocationDetailsData mapRow(final ResultSet rs, final int rowNum)throws SQLException {

			final Long id = rs.getLong("id");
			final String serialNum = rs.getString("serialNo");
			final String itemDescription = rs.getString("itemDescription");
			final String allocationType = rs.getString("allocationType");

			return new AllocationDetailsData(id, itemDescription, serialNum,null, null, allocationType, null, null, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see getDisconnectedOrderHardwareDetails(java.lang.Long, java.lang.Long)
	 */
	@Override
	public AllocationDetailsData getDisconnectedOrderHardwareDetails(final Long orderId, final Long serviceId, final Long clientId) {

		try {

			final ClientOrderMapper mapper = new ClientOrderMapper();
			final String sql = " SELECT  a.id AS id,a.order_id AS orderId,a.service_id as serviceId,a.plan_id as planId,a.client_id AS clientId,i.provisioning_serialno AS serialNum "
					+ " FROM  b_association a, b_item_detail i WHERE a.order_id = ? AND a.service_id = ? AND a.hw_serial_no = i.serial_no "
					+ " AND a.id = (SELECT MAX(id) FROM  b_association a WHERE a.client_id = ? AND a.service_id = ? AND a.is_deleted = 'Y')";
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] {orderId, serviceId, clientId, serviceId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	
	@Override
	public List<AllocationDetailsData> getTheOldOrderHardwareItemDetails(final Long orderId,final String serialNumber) {
		try {
			final ClientOldOrderMapper mapper = new ClientOldOrderMapper();
			 String sql =null;
			if(serialNumber!=null){
				  sql = " Select * from ( select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,id.provisioning_serialno AS serialNum,id.serial_no AS serialno,a.client_id AS clientId" +
					  		" FROM b_association a, b_item_detail id WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='N' and a.hw_serial_no=? " +
					  		" and allocation_type='ALLOT'" +
					  		" union all" +
					  		" select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,o.provisioning_serial_number AS serialNum,o.serial_number AS serialno,a.client_id AS clientId" +
					  		" FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no and  a.hw_serial_no=? " +
					  		" AND a.is_deleted = 'N' and o.is_deleted = 'N' and allocation_type='OWNED') a ";
					  		
					  
				return jdbcTemplate.query(sql, mapper, new Object[] {  orderId,serialNumber,orderId,serialNumber});
				
			}else{
				  sql = " Select * from ( select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,id.provisioning_serialno AS serialNum,id.serial_no AS serialno,a.client_id AS clientId" +
				  		" FROM b_association a, b_item_detail id WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='Y'" +
				  		" and allocation_type='ALLOT'" +
				  		" union all" +
				  		" select a.id AS id,a.order_id AS orderId,a.plan_id as planId,a.service_id as serviceId,o.provisioning_serial_number AS serialNum,o.serial_number AS serialno,a.client_id AS clientId" +
				  		" FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no " +
				  		" AND a.is_deleted = 'Y' and o.is_deleted = 'N' and allocation_type='OWNED') a ";
				  		
				  
			return jdbcTemplate.query(sql, mapper, new Object[] {  orderId,orderId });
			}
			} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class ClientOldOrderMapper implements RowMapper<AllocationDetailsData> {

		/*
		 * public String clientAssociationLookupSchema() { 
		 * return " a.id AS id,a.order_id AS orderId,id.provisioning_serialno AS serialNum,a.client_id AS clientId FROM b_association a, b_item_detail id"
		 * + " WHERE a.order_id =? and id.serial_no=a.hw_serial_no and a.is_deleted='N' limit 1" ; }
		 * 
		 * public String clientOwnHwAssociationLookupSchema() {
		 * 
		 * return "  a.id AS id,a.order_id AS orderId,o.provisioning_serial_number AS serialNum,a.client_id AS clientId"
		 * + " FROM b_association a, b_owned_hardware o WHERE a.order_id =?  AND o.serial_number = a.hw_serial_no   "
		 * + " AND a.is_deleted = 'N' and o.is_deleted = 'N' limit 1"; }
		 */

		/*
		 * public String clientDeAssociationLookupSchema() { 
		 * return " a.id AS id, a.order_id AS orderId,a.client_id AS clientId,i.provisioning_serialno as serialNum FROM b_association a, b_item_detail i  "
		 * + " WHERE order_id = ? and a.hw_serial_no=i.serial_no AND a.id = (SELECT MAX(id) FROM b_association a WHERE  a.client_id =?  and a.is_deleted = 'Y')  limit 1"; }
		 * 
		 * public String clientOwnHwDeAssociationLookupSchema() { 
		 * return " a.id AS id,a.order_id AS orderId,a.client_id AS clientId,a.hw_serial_no AS serialNum FROM b_association a  "
		 * + " WHERE order_id =? AND a.id = (SELECT MAX(id) FROM b_association a  WHERE a.client_id =? AND a.is_deleted = 'Y') LIMIT 1" ; }
		 */

		@Override
		public AllocationDetailsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

	    final Long id = rs.getLong("id");	
		final Long serviceId = rs.getLong("serviceId");
		final Long orderId = rs.getLong("orderId");
		final Long planId = rs.getLong("planId");
		final String serialNum = rs.getString("serialNum");
		final String serialno = rs.getString("serialno");
		final Long clientId = rs.getLong("clientId");
		
		return new AllocationDetailsData(id,serviceId,planId,orderId,serialNum,clientId,serialno);
		  }
	}

}
