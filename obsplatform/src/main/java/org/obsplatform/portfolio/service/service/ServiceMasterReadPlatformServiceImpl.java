package org.obsplatform.portfolio.service.service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.service.data.ServiceMasterData;
import org.obsplatform.portfolio.service.data.ServiceMasterOptionsData;
import org.obsplatform.portfolio.service.data.ServiceStatusEnumaration;
import org.obsplatform.portfolio.service.data.ServiceTypeEnum;
import org.obsplatform.portfolio.service.data.ServiceUnitTypeEnum;
import org.obsplatform.portfolio.service.data.ServiceUnitTypeEnumaration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class ServiceMasterReadPlatformServiceImpl implements  ServiceMasterReadPlatformService{

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ServiceMasterData> paginationHelper = new PaginationHelper<ServiceMasterData>();

	@Autowired
	public  ServiceMasterReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<ServiceMasterOptionsData> retrieveAllServiceMasterOptionsData() {
		
		this.context.authenticatedUser();
		final ServiceMasterMapper mapper = new ServiceMasterMapper();
		final String sql = "select " + mapper.schema();
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});

	}

	protected static final class ServiceMasterMapper implements RowMapper<ServiceMasterOptionsData> {

		@Override
		public ServiceMasterOptionsData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			
			final String discounType = rs.getString("serviceType");
			final String discountValue=rs.getString("categoryType");
			return new ServiceMasterOptionsData(discounType,discountValue);

		}

		public String schema() {
			return "d.servicetype as servicetype , d.categorytype as categorytype from m_servicemaster_type d";
		}
	}

	/*@Override
	public Page<ServiceMasterData> retrieveServices(SearchSqlQuery searchCodes) {
		
		this.context.authenticatedUser();
		final ServiceMapper mapper = new ServiceMapper();
		final String sql = "select " + mapper.schema()+" where d.is_deleted='n' ";
		StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append(sql);
		
		if (searchCodes.isLimited()) {
            sqlBuilder.append(" limit ").append(searchCodes.getLimit());
        }
        if (searchCodes.isOffset()) {
            sqlBuilder.append(" offset ").append(searchCodes.getOffset());
        }

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
	            new Object[] {}, mapper);

	}*/
public Page<ServiceMasterData> retrieveServices(SearchSqlQuery searchCodes) {
		
		this.context.authenticatedUser();
		final ServiceMapper mapper = new ServiceMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
	    final String sql = "select SQL_CALC_FOUND_ROWS " + mapper.schema()+" where d.is_deleted='n' ";
		sqlBuilder.append(sql);
		
		/*search text in service_code and service_discription*/ 
		String sqlSearch = searchCodes.getSqlSearch();
		String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch=sqlSearch.trim();
	    	extraCriteria = " and (d.service_code like '%"+sqlSearch+"%' OR" 
	    			+ " d.service_description like '%"+sqlSearch+"%' )";
	    			
	    }
	        sqlBuilder.append(extraCriteria);
	        
		if (searchCodes.isLimited()) {
            sqlBuilder.append(" limit ").append(searchCodes.getLimit());
        }
        if (searchCodes.isOffset()) {
            sqlBuilder.append(" offset ").append(searchCodes.getOffset());
        }

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
	            new Object[] {}, mapper);

	}

	protected static final class ServiceMapper implements RowMapper<ServiceMasterData> {

		@Override
		public ServiceMasterData mapRow(final ResultSet rs, final int rowNum)throws SQLException {
			
			final Long id=rs.getLong("id");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceDescription=rs.getString("serviceDescription");
			final String serviceType=rs.getString("serviceType");
			final String serviceUnitType=rs.getString("serviceUnitType");
			final String status=rs.getString("status");
			final String isOptional=rs.getString("isOptional");
			final String isAutoProvision=rs.getString("isAuto");

			return new ServiceMasterData(id,serviceCode,serviceDescription,serviceType,serviceUnitType,status,isOptional,isAutoProvision);

		}


		public String schema() {
			return "d.id AS id,d.service_code AS serviceCode,d.service_description AS serviceDescription,d.service_type AS serviceType," +
					"d.service_unittype as serviceUnitType,d.status as status,d.is_optional as isOptional,d.is_auto as isAuto "+
					" FROM b_service d";
		}

}

	@Override
	public ServiceMasterData retrieveIndividualService(final Long serviceId) {
		
		final ServiceMapper mapper = new ServiceMapper();
		final String sql = "select " + mapper.schema()+" where d.id="+serviceId;
		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {});

	}

	private static final class ServiceDetailsMapper implements RowMapper<ServiceData> {

		public String schema() {
			return "da.id as id, da.service_code as service_code, da.service_description as service_description "
					+ " from b_service da where da.is_deleted='N' ";

		}

		@Override
		public ServiceData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

			Long id = rs.getLong("id");
			String serviceCode = rs.getString("service_code");
			String serviceDescription = rs.getString("service_description");
			return new ServiceData(id,null,null,null,serviceCode, serviceDescription,null,null,null,null);

		}
	}

	@Override
	public List<EnumOptionData> retrieveServicesTypes() {
		
		final EnumOptionData tv = ServiceStatusEnumaration.serviceType(ServiceTypeEnum.TV);
		final EnumOptionData bb= ServiceStatusEnumaration.serviceType(ServiceTypeEnum.BB);
		final EnumOptionData voId= ServiceStatusEnumaration.serviceType(ServiceTypeEnum.VOIP);
		final EnumOptionData iptv= ServiceStatusEnumaration.serviceType(ServiceTypeEnum.IPTV);
		final EnumOptionData vod= ServiceStatusEnumaration.serviceType(ServiceTypeEnum.VOD);
		final EnumOptionData none= ServiceStatusEnumaration.serviceType(ServiceTypeEnum.NONE);
		
		final List<EnumOptionData> categotyType = Arrays.asList(tv,bb,voId,iptv,vod,none);
			return categotyType;
	}

	@Override
	public List<EnumOptionData> retrieveServiceUnitType() {
		final EnumOptionData onOff = ServiceUnitTypeEnumaration.serviceUnitType(ServiceUnitTypeEnum.ON_OFF);
		final EnumOptionData scheme= ServiceUnitTypeEnumaration.serviceUnitType(ServiceUnitTypeEnum.SCHEME);
		final EnumOptionData quantity = ServiceUnitTypeEnumaration.serviceUnitType(ServiceUnitTypeEnum.QUANTITY);
		final List<EnumOptionData> categotyType = Arrays.asList(onOff,scheme,quantity);
			return categotyType;
	}

	@Override
	public List<ServiceData> retrieveAllServices(String serviceType) {

		context.authenticatedUser();
		ServiceDetailsMapper mapper = new ServiceDetailsMapper();
		String sql = "select " + mapper.schema()+" and da.is_optional = ?";
		return this.jdbcTemplate.query(sql, mapper, new Object[] {serviceType});

	  }
}
