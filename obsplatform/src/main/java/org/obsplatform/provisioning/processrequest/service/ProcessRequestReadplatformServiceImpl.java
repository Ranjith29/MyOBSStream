package org.obsplatform.provisioning.processrequest.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.core.service.RoutingDataSourceService;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.infrastructure.security.service.TenantDetailsService;
import org.obsplatform.provisioning.processrequest.data.ProcessingDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class ProcessRequestReadplatformServiceImpl implements ProcessRequestReadplatformService{

	private final TenantDetailsService tenantDetailsService;
	private final RoutingDataSourceService dataSourcePerTenantService;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ProcessRequestReadplatformServiceImpl(final RoutingDataSourceService dataSourcePerTenantService,
			final TenantDetailsService tenantDetailsService,final RoutingDataSource dataSource) {
		
		this.dataSourcePerTenantService = dataSourcePerTenantService;
		this.tenantDetailsService = tenantDetailsService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	@Override
	public List<ProcessingDetailsData> retrieveProcessingDetails() {
		try {

			final ObsPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");
			ThreadLocalContextUtil.setTenant(tenant);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
			final ClientOrderMapper mapper = new ClientOrderMapper();

			final String sql = "select " + mapper.processLookupSchema() + " where p.is_processed='Y' and p.is_notify='N' limit 100";

			return jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	private static final class ClientOrderMapper implements RowMapper<ProcessingDetailsData> {

		public String processLookupSchema() {
			return "p.id as id,p.order_id as orderId,p.provisioing_system as provisionigSys,p.request_type as requestType  FROM b_process_request p";
		}

		@Override
		public ProcessingDetailsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long orderId = rs.getLong("orderId");
			final String provisionigSys = rs.getString("provisionigSys");
			final String requestType = rs.getString("requestType");

			return new ProcessingDetailsData(id, orderId, provisionigSys,requestType);
		}
	}

	@Override
	public List<ProcessingDetailsData> retrieveUnProcessingDetails() {

		try {

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
			final ClientOrderMapper mapper = new ClientOrderMapper();
			final String sql = "select " + mapper.processLookupSchema()+ " where p.is_processed='N'";
			return jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public Long retrievelatestReqId(Long clientId, String oldHardware) {

		try {

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveDataSource());
			final String sql = "SELECT max(p.id) FROM b_process_request p, b_process_request_detail pr WHERE p.id = pr.processrequest_id AND "
					+ " pr.hardware_id = ?  and p.client_id=? AND p.is_processed ='F' limit 1";

			return jdbcTemplate.queryForLong(sql, new Object[] { oldHardware,clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}
	
	@Override
	public Boolean retrieveChangeCredentialsProcessRequests(final Long clientId) {
		
		 boolean result = false;
		 final String sql="SELECT count(id) FROM b_process_request p WHERE p.is_processed = 'N'  AND p.is_notify='N' AND p.request_type='CHANGE CREDENTIALS' AND p.client_id= ? ";
		 final int count= this.jdbcTemplate.queryForObject(sql, Integer.class,new Object[]{clientId});
		 if(count > 0){
			 result = true;
		 }
		 return result;
		 
	}

}
