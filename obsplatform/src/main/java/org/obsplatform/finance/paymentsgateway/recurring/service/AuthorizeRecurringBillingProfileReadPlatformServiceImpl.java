package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.finance.paymentsgateway.recurring.data.RecurringData;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AuthorizeRecurringBillingProfileReadPlatformServiceImpl implements AuthorizeRecurringBillingProfileReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public AuthorizeRecurringBillingProfileReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<RecurringData> retrieveRecurringData(Long clientId) {
		try {
			context.authenticatedUser();
			final RecurringMapper mapper = new RecurringMapper();
			final String sql = "select " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] {clientId});

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}
	private static final class RecurringMapper implements RowMapper<RecurringData> {

		public String schema() {
			return " br.id as id,br.subscriber_id as subscriberId,br.order_id as orderId,br.gateway_name as pgName"+ 
					" from b_recurring br where br.client_id = ? and br.is_deleted ='N' ";
		}

		@Override
		public RecurringData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String subscriberId = rs.getString("subscriberId");
			final Long orderId = rs.getLong("orderId");
			final String pgName = rs.getString("pgName");

			return new RecurringData(id, subscriberId, orderId, pgName);
		}
   }
}
