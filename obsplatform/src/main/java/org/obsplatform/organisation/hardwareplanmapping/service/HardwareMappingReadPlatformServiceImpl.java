package org.obsplatform.organisation.hardwareplanmapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.hardwareplanmapping.data.HardwareMappingDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class HardwareMappingReadPlatformServiceImpl implements HardwareMappingReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public HardwareMappingReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<HardwareMappingDetailsData> getPlanDetailsByItemCode(final String itemCode, 
			final Long clientId) {
		
		try {
			this.context.authenticatedUser();
			final HardwareMapper mapper = new HardwareMapper();
			final String sql = "select" + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {
					itemCode, clientId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	private static final class HardwareMapper implements
			RowMapper<HardwareMappingDetailsData> {

		public String schema() {

			return " p.id AS planId, p.plan_code AS planCode, o.id AS orderId FROM b_orders o,b_plan_master p,b_hw_plan_mapping phw"
					+ "  WHERE p.id = o.plan_id  AND phw.plan_code = p.plan_code and phw.item_code =? and o.client_id=?";
		}

		@Override
		public HardwareMappingDetailsData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			Long planId = rs.getLong("planId");
			Long orderId = rs.getLong("orderId");
			String planCode = rs.getString("planCode");

			return new HardwareMappingDetailsData(planId, orderId, planCode);
		}

	}

}
