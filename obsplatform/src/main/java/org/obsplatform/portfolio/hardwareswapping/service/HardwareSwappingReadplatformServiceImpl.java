package org.obsplatform.portfolio.hardwareswapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class HardwareSwappingReadplatformServiceImpl implements HardwareSwappingReadplatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public HardwareSwappingReadplatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		this.context=context;
		this.jdbcTemplate=new JdbcTemplate(dataSource);
	}
	
	@Override
	public Boolean retrieveingDisconnectionOrders(String serialNumber) {
		
		this.context.authenticatedUser();
		boolean result = false;
		final String sql="select count(ba.id) AS count from b_association ba " +
							" JOIN b_orders bo ON ba.order_id = bo.id AND ba.client_id=bo.client_id " +
							" where ba.hw_serial_no = ? AND bo.order_status = 3 " +
							" AND user_action in ('DISCONNECTION','CHANGE_PLAN') " +
							" AND ba.is_deleted = 'N' AND bo.is_deleted = 'N'";
		
		 final int count=	this.jdbcTemplate.queryForObject(sql, Integer.class,new Object[]{serialNumber});
		 if(count > 0){
			 result = true;
		 }
		 return result;
	}

	@Override
	public List<AssociationData> retrievingAllAssociations(Long clientId,String serialNumber,Long orderId) {

		this.context.authenticatedUser();
		AssociationMapper mapper = new AssociationMapper();
		if(Long.valueOf(0).equals(orderId)){
			
			String sql = "select "+mapper.schema()+" where client_id = ? and hw_serial_no = ? and is_deleted='N' ";
			return this.jdbcTemplate.query(sql, mapper,new Object[]{clientId,serialNumber});
			
		}else{
			
			String sql = "select "+mapper.schema()+" where client_id = ? and order_id = ? and hw_serial_no = ? and is_deleted='N' ";
			return this.jdbcTemplate.query(sql, mapper,new Object[]{clientId,orderId,serialNumber});
		}
	}
	
	private static final class AssociationMapper implements RowMapper<AssociationData>{

		public String schema(){
			return " id as id,client_id as clientId,order_id as orderId,plan_id as planId,hw_serial_no as serialNo, " +
					" allocation_type as allocationType,service_id as serviceId from b_association ";

		}

		@Override
		public AssociationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long clientId = rs.getLong("clientId");
			final Long orderId = rs.getLong("orderId");
			final Long planId = rs.getLong("planId");
			final String serialNo = rs.getString("serialNo");
			final String allocationType = rs.getString("allocationType");
			final Long serviceId =rs.getLong("serviceId");

			return new AssociationData(id,clientId,orderId,planId,serialNo,allocationType,serviceId);
		}



	}

	@Override
	public Boolean retrieveingPendingOrders(String serialNumber) {
		
		this.context.authenticatedUser();
		boolean result = false;
		final String sql="select count(ba.id) AS count from b_association ba " +
							" JOIN b_orders bo ON ba.order_id = bo.id AND ba.client_id=bo.client_id " +
							" JOIN b_process_request pr ON ba.client_id = pr.client_id AND ba.order_id = pr.order_id " +
							" where ba.hw_serial_no = ? AND bo.order_status = 4 " +
							" AND ba.is_deleted = 'N' AND bo.is_deleted = 'N' AND pr.is_processed = 'N'";
		
		 final int count=	this.jdbcTemplate.queryForObject(sql, Integer.class,new Object[]{serialNumber});
		 if(count > 0){
			 result = true;
		 }
		 return result;
	}

}
