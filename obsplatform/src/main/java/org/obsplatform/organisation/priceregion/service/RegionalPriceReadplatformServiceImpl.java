package org.obsplatform.organisation.priceregion.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.priceregion.data.PriceRegionData;
import org.obsplatform.organisation.priceregion.service.RegionalPriceReadplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class RegionalPriceReadplatformServiceImpl implements RegionalPriceReadplatformService{
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
@Autowired
public RegionalPriceReadplatformServiceImpl(final RoutingDataSource dataSource,final PlatformSecurityContext context){
	this.jdbcTemplate=new JdbcTemplate(dataSource);
	this.context=context;
}

	@Override
	public List<PriceRegionData> getPriceRegionsDetails() {
	
		try{
			this.context.authenticatedUser();
			PriceRegionMapper mapper = new PriceRegionMapper();
			String sql="select "+mapper.schema();
			return this.jdbcTemplate.query(sql,mapper,new Object[]{});
			
			
			
			
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}
		
	}
	
	
	
	 
	
	 
	private static final class PriceRegionMapper implements RowMapper<PriceRegionData> {

		public String schema() {
			return "  pr.id as id,pr.priceregion_code as priceregionCode,pr.priceregion_name as priceRegion FROM b_priceregion_master pr where pr.is_deleted='N'";

		}

		@Override
		public PriceRegionData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String priceregionCode = rs.getString("priceregionCode");
			String priceRegion = rs.getString("priceRegion");

			return new PriceRegionData(id,priceregionCode,priceRegion);

		}
	}

	@Override
	public PriceRegionData getTheClientRegionDetails(Long clientId) {
		
		try{
			
			this.context.authenticatedUser();
			ClientRegionMapper mapper = new ClientRegionMapper();
			String sql="select "+mapper.schema();
			return this.jdbcTemplate.queryForObject(sql,mapper,new Object[]{ clientId });
			
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}


	}
	
	private static final class ClientRegionMapper implements RowMapper<PriceRegionData> {

		public String schema() {
			return " pm.id as id,pm.priceregion_code as priceRegion FROM b_client_address ca, b_priceregion_detail pd," +
					" b_priceregion_master pm,b_state s  WHERE  ca.client_id = ? AND ca.state = s.state_name" +
					" AND s.id = pd.state_id AND pm.id = pd.priceregion_id";

		}

		@Override
		public PriceRegionData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String priceRegion = rs.getString("priceRegion");

			return new PriceRegionData(id,null,priceRegion);

		}
	}
}
