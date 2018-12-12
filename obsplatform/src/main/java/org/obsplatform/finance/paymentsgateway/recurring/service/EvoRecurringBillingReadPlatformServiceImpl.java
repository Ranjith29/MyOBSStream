package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.finance.paymentsgateway.recurring.data.EvoBatchProcessData;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class EvoRecurringBillingReadPlatformServiceImpl implements EvoRecurringBillingReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public EvoRecurringBillingReadPlatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	@Override
	public List<EvoBatchProcessData> getUploadedFile() {

		try {
			
			final EvoBatchFileMapper mapper = new EvoBatchFileMapper();
			final String sql = "select " + mapper.evofileLookupSchema() + " where be.is_uploaded='Y' and be.is_downloaded='N'" ;
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
			
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		
	}
	
	private static final class EvoBatchFileMapper implements RowMapper<EvoBatchProcessData> {

		public String evofileLookupSchema() {
			return " be.id as id,be.input_filename as fileName,be.path as filePath,be.description as description from b_evo_batchprocess be  ";
		}

		@Override
		public EvoBatchProcessData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			final Long id = rs.getLong("id");
			final String fileName = rs.getString("fileName");
			final String filePath = rs.getString("filePath");
			final String description = rs.getString("description");

			return new EvoBatchProcessData(id, fileName, filePath, description);
		}
	}

}
