package org.obsplatform.billing.chargevariant.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantDetailsData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class ChargeVariantReadPlatformServiceImpl implements ChargeVariantReadPlatformService{
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public ChargeVariantReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
	
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	@Override
	public List<ChargeVariantData> retrieveAllChargeVariantData() {

		try {
			context.authenticatedUser();
			final ChargeVarianttMapper mapper = new ChargeVarianttMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}
	
	
	private static final class ChargeVarianttMapper implements RowMapper<ChargeVariantData> {

		public String schema() {
			return " c.id as id,c.chargevariant_code as chargeVariantCode,c.start_date as startDate,c.end_date as endDate,c.status as status"
					+ " from b_chargevariant c where c.is_delete ='N'";

		}


		@Override
		public ChargeVariantData mapRow(final ResultSet resultSet,final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String chargeVariantCode = resultSet.getString("chargeVariantCode");
			final LocalDate startDate = JdbcSupport.getLocalDate(resultSet,"startDate");
			final LocalDate endDate = JdbcSupport.getLocalDate(resultSet,"endDate");
			final String status = resultSet.getString("status");
			return new ChargeVariantData(id, chargeVariantCode, startDate, endDate, status);

		}
	}


	@Override
	public ChargeVariantData retrieveChargeVariantData(Long variantId) {
		try {
			context.authenticatedUser();
			final ChargeVarianttMapper mapper = new ChargeVarianttMapper();
			final String sql = "select " + mapper.schema()+" and c.id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {variantId});
			
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}


	@Override
	public List<ChargeVariantDetailsData> retrieveAllChargeVariantDetails(final Long variantId) {
		
		try{
		   this.context.authenticatedUser();
		   final ChargeVariantDetailsMapper mapper = new ChargeVariantDetailsMapper();
		   final String sql = "select "+mapper.schema();
		   return this.jdbcTemplate.query(sql, mapper, new Object[] {variantId});
		}catch(EmptyResultDataAccessException dve){
			return null;	
		}
	}
	
	private static final class ChargeVariantDetailsMapper implements RowMapper<ChargeVariantDetailsData>{

		public String schema() {
			return "   cd.id as id,cd.chargevariant_id as chargeVariantId,cd.variant_type as variantType,"
				+  "  cd.from_range as fromRange,cd.to_range as toRange,cd.amount_type as amountType,cd.amount"
				+ "   from b_chargevariant_detail cd  where cd.chargevariant_id=? and cd.is_deleted ='N'";
			}
		
		@Override
		public ChargeVariantDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long id = rs.getLong("id");
			final Long chargeVariantId = rs.getLong("chargeVariantId");
			final String variantType = rs.getString("variantType");
			final Long to = rs.getLong("toRange");
			final Long from = rs.getLong("fromRange");
			final String amountType = rs.getString("amountType");
			final BigDecimal amount = rs.getBigDecimal("amount");
			return new ChargeVariantDetailsData(id, chargeVariantId, variantType, from, to, amountType, amount);
		}
		
	}

	}


