package org.obsplatform.provisioning.wifimaster.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.provisioning.wifimaster.data.WifiData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author anil
 *
 */
@Service
public class WifiMasterReadPlatformServiceImp implements WifiMasterReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public WifiMasterReadPlatformServiceImp(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	/* Retriview all details of wifi Master
	 */
	@Override
	public List<WifiData> wifiAllDetailsData() {

		final WifiMasterDataMapper mapper = new WifiMasterDataMapper();
		
		String sql = "Select " + mapper.schema() + " where wd.is_deleted='N'";
		
		return jdbcTemplate.query(sql,mapper,new Object[]{});
	}
	
	@Override
	public List<WifiData> WifiDataGetByClientId(final Long clientId) {

		final WifiMasterDataMapper mapper = new WifiMasterDataMapper();
		
		String sql = "Select " + mapper.schema() + " where wd.is_deleted='N' AND wd.client_id = ?";
		
		return jdbcTemplate.query(sql,mapper,new Object[]{ clientId });
	}
	

	/* 
	 * @see #retrievedSingleWifiData(java.lang.BigInt)
	 */
	@Override
	public WifiData retrievedSingleWifiData(final Long id) {
		
		try{
			this.context.authenticatedUser();
        final WifiMasterDataMapper mapper = new WifiMasterDataMapper();
		final String sql = "Select " + mapper.schema()+ " where wd.client_id = ?";
		
		return jdbcTemplate.queryForObject(sql, mapper, new Object[] { id });
		
		}catch (EmptyResultDataAccessException accessException) {
			return null;
		}
		
	}
	
	@Override
	public WifiData getByOrderId(final Long id,final Long orderId) {
		
		try{
			this.context.authenticatedUser();
        final WifiMasterDataMapper mapper = new WifiMasterDataMapper();
		final String sql = "Select " + mapper.schema()+ " where wd.client_id = ? AND wd.order_id = ? ";
		
		return jdbcTemplate.queryForObject(sql, mapper, new Object[] { id,orderId });
		
		}catch (EmptyResultDataAccessException accessException) {
			return null;
		}
		
	}
	
	
	
	

	private class WifiMasterDataMapper implements RowMapper<WifiData> {
		
		
		public String schema() {

			return "wd.id AS id,"
					+"wd.client_id AS clientId,"
					+"wd.ssid AS ssid,"
					+"wd.service_type AS serviceType,"
					+"wd.wifi_password AS wifiPassword,"
					+"wd.order_id AS orderId,"
					+"wd.service_id AS serviceId,"
					+"wd.is_deleted AS is_deleted FROM b_wifi_details wd ";
		}

		@Override
		public WifiData mapRow(ResultSet rs, int rowNum) throws SQLException {
			final Long id = rs.getLong("id");
			final Long clientId = rs.getLong("clientId");
			final String ssid = rs.getString("ssid");
			final String wifiPassword = rs.getString("wifiPassword");
			final String serviceType = rs.getString("serviceType");
			final Long orderId = rs.getLong("orderId");
			final Long serviceId = rs.getLong("serviceId");
			
			return new WifiData(id, clientId, ssid, wifiPassword, serviceType,orderId,serviceId);
		}

	}

}

