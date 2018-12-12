/**
 * 
 */
package org.obsplatform.finance.usagecharges.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author Ranjith
 * @param <UsageChargesData>
 *
 */
@Service
public class UsageChargesReadPlatformServiceImpl implements UsageChargesReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public UsageChargesReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/** (non-Javadoc)
	 * @see retrieveOrderCdrData(Long, Long)
	 */
	@Override
	public List<UsageChargesData> retrieveOrderCdrData(Long clientId, Long orderId) {
		
		final OrderChargesMapper chargesMapper=new OrderChargesMapper();
		final String sql="SELECT "+chargesMapper.schema();		
		return this.jdbcTemplate.query(sql,chargesMapper,new Object[]{clientId,orderId});
	}

     private  static final class OrderChargesMapper implements RowMapper<UsageChargesData> {
	
		@Override
		public UsageChargesData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long id=rs.getLong("id");
			final String number=rs.getString("number");
			final BigDecimal totalCost = rs.getBigDecimal("totalCost");
			final BigDecimal totalDuration = rs.getBigDecimal("totalDuration");
			final LocalDate chargeDate = JdbcSupport.getLocalDate(rs, "chargeDate");
			
			return new UsageChargesData(id,number,chargeDate, totalCost,totalDuration);
		}

		public String schema() {
			
			return " uc.id As id,number AS number,charge_date AS chargeDate,total_cost AS totalCost, total_duration AS totalDuration  FROM b_usage_charge uc,b_association a " 
					+" WHERE uc.number = a.hw_serial_no AND uc.client_id = a.client_id AND a.is_deleted='N' "
                    +" AND uc.charge_id is null AND uc.client_id = ? AND a.order_id = ? ";
		}
     }

}