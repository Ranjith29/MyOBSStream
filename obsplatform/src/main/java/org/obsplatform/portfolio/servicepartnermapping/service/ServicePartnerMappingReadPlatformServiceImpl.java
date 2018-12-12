package org.obsplatform.portfolio.servicepartnermapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.portfolio.servicepartnermapping.data.ServicePartnerMappingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author Naresh
 * 
 */
@Service
public class ServicePartnerMappingReadPlatformServiceImpl implements ServicePartnerMappingReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PaginationHelper<ServicePartnerMappingData> paginationHelper = new PaginationHelper<ServicePartnerMappingData>();

	@Autowired
	public ServicePartnerMappingReadPlatformServiceImpl(final RoutingDataSource RoutingDataSource) {
		this.jdbcTemplate = new JdbcTemplate(RoutingDataSource);
	}

	@Override
	public Page<ServicePartnerMappingData> getAllServicePartnerMappingData(final SearchSqlQuery searchCodes) {

		ServicePartnerMappingMapper mapper = new ServicePartnerMappingMapper();
		String sql = "select SQL_CALC_FOUND_ROWS " + mapper.schema();
		StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append(sql);
		if (searchCodes.isLimited()) {
			sqlBuilder.append(" limit ").append(searchCodes.getLimit());
		}
		if (searchCodes.isOffset()) {
			sqlBuilder.append(" offset ").append(searchCodes.getOffset());
		}

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()", sqlBuilder.toString(), new Object[] {}, mapper);
	}

	@Override
	public ServicePartnerMappingData getServicePtrMappingById(final Long servicePtrMappId) {

		final ServicePartnerMappingMapper mapper = new ServicePartnerMappingMapper();
		final String sql = "select " + mapper.schema() + " and spm.id = ? ";

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { servicePtrMappId });
	}

	private static final class ServicePartnerMappingMapper implements RowMapper<ServicePartnerMappingData> {

		public String schema() {

			return " spm.id as id, spm.partner_name as partnerName, spm.service_id as serviceId, bs.service_code as serviceCode, "
					+ " bs.service_description as serviceDescription, bs.service_type as serviceType "
					+ " from b_service_partner_mapping spm "
					+ " join b_service bs ON (bs.id = spm.service_id and bs.is_deleted = 'N') where spm.is_deleted = 'N'";
		}

		@Override
		public ServicePartnerMappingData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long serviceId = rs.getLong("serviceId");
			final String partnerName = rs.getString("partnerName");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceDescription = rs.getString("serviceDescription");
			final String serviceType = rs.getString("serviceType");

			return new ServicePartnerMappingData(id, serviceId, partnerName, serviceCode, serviceDescription, serviceType);
		}
	}

	@Override
	public List<ServicePartnerMappingData> getServiceCode() {

		final ServiceCodeDataMapper mapper = new ServiceCodeDataMapper();
		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class ServiceCodeDataMapper implements RowMapper<ServicePartnerMappingData> {

		public String schema() {
			return " bs.id as id, bs.service_code as serviceCode, bs.service_type as serviceType, "
					+ " bs.service_description as serviceDescription from b_service bs where bs.is_deleted = 'n' and bs.status = 'ACTIVE' "
					+ " and bs.id not in (select spm.service_id from b_service_partner_mapping spm where spm.service_id = bs.id and spm.is_deleted = 'N') ";
		}

		@Override
		public ServicePartnerMappingData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceType = rs.getString("serviceType");
			final String serviceDescription = rs.getString("serviceDescription");
			return new ServicePartnerMappingData(id, serviceCode, serviceType, serviceDescription);
		}
	}

	@Override
	public List<ServicePartnerMappingData> getServiceCode(final Long serviceId) {

		final ServiceCodeDataByIdMapper mapper = new ServiceCodeDataByIdMapper();
		final String sql = "select " + mapper.schema() + " and not spm.service_id = ? ) ";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { serviceId });
	}

	private static final class ServiceCodeDataByIdMapper implements RowMapper<ServicePartnerMappingData> {

		public String schema() {
			return " bs.id as id, bs.service_code as serviceCode, bs.service_type as serviceType, "
					+ " bs.service_description as serviceDescription from b_service bs where bs.is_deleted = 'n' and bs.status = 'ACTIVE' "
					+ " and bs.id not in (select spm.service_id from b_service_partner_mapping spm where spm.service_id = bs.id and spm.is_deleted = 'N'";
		}

		@Override
		public ServicePartnerMappingData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String serviceCode = rs.getString("serviceCode");
			final String serviceType = rs.getString("serviceType");
			final String serviceDescription = rs.getString("serviceDescription");
			
			return new ServicePartnerMappingData(id, serviceCode, serviceType, serviceDescription);
		}
	}

	@Override
	public List<ServicePartnerMappingData> getPartnerNames() {

		final PartnerNamesMapper mapper = new PartnerNamesMapper();
		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class PartnerNamesMapper implements RowMapper<ServicePartnerMappingData> {

		public String schema() {
			return "mo.id as id, mo.name as name, " 
					+ "mo.partner_type_id as partnertypeid, " 
					+ " m.code_value as codevalue  from m_office  mo " 
					+ "join m_code_value m on mo.partner_type_id = m.id " 
					+ " where mo.partner_type_id is not null ";
		}

		@Override
		public ServicePartnerMappingData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String name = rs.getString("name");
			final Long partnertypeid = rs.getLong("partnertypeid");
			final String codevalue = rs.getString("codevalue");
			return new ServicePartnerMappingData(id, name, partnertypeid, codevalue);
		}
	}

}
