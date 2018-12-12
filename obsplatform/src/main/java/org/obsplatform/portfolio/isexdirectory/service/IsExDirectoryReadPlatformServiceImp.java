package org.obsplatform.portfolio.isexdirectory.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.isexdirectory.data.IsExDirectoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Naresh
 * 
 */
@Service
public class IsExDirectoryReadPlatformServiceImp implements IsExDirectoryReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public IsExDirectoryReadPlatformServiceImp(final PlatformSecurityContext context, final RoutingDataSource dataSource) {

		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public IsExDirectoryData retrieveIsExDirectoryByOrderId(final Long orderId) {

		try {
			this.context.authenticatedUser();
			final IsExDirectoryDataMapper mapper = new IsExDirectoryDataMapper();
			final String sql = "Select " + mapper.schema() + "where bie.order_id = ? and bie.is_deleted = 'N' ";

			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { orderId });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private class IsExDirectoryDataMapper implements RowMapper<IsExDirectoryData> {

		public String schema() {

			return  " bie.id AS id, bie.client_id AS clientId, bie.order_id AS orderId, bie.plan_id AS planId, bie.service_id AS serviceId, " +
					" bie.is_ex_directory AS isExDirectory, bie.is_number_with_held AS isNumberWithHeld, bie.is_umee_app as isUmeeApp " +
					" FROM b_exdirectory bie ";
		}

		@Override
		public IsExDirectoryData mapRow(ResultSet rs, int rowNum) throws SQLException {
			final Long id = rs.getLong("id");
			final Long clientId = rs.getLong("clientId");
			final Long orderId = rs.getLong("orderId");
			final Long planId = rs.getLong("planId");
			final Long serviceId = rs.getLong("serviceId");
			final boolean isExDirectory = rs.getBoolean("isExDirectory");
			final boolean isNumberWithHeld = rs.getBoolean("isNumberWithHeld");
			final boolean isUmeeApp = rs.getBoolean("isUmeeApp");

			return new IsExDirectoryData(id, clientId, orderId, planId, serviceId, isExDirectory, isNumberWithHeld, isUmeeApp);
		}

	}

}
