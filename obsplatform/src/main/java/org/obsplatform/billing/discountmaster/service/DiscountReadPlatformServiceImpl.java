package org.obsplatform.billing.discountmaster.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.discountmaster.data.DiscountDetailData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author Ranjith
 * 
 */
@Service
public class DiscountReadPlatformServiceImpl implements DiscountReadPlatformService {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public DiscountReadPlatformServiceImpl(final RoutingDataSource dataSource) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * (non-Java doc)
	 * 
	 * @see #retrieveAllDiscounts()
	 */
	@Override
	public List<DiscountMasterData> retrieveAllDiscounts() {

		try {
			final DiscountMapper mapper = new DiscountMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	private static final class DiscountMapper implements RowMapper<DiscountMasterData> {

		public String schema() {
			return "ds.id as id, ds.discount_code as discountCode, ds.discount_description as discountDescription,"
					+ "ds.discount_type as discountType, ds.discount_rate as discountRate, ds.start_date as startDate,"
					+ "ds.end_date as endDate,ds.discount_status as discountStatus from b_discount_master ds where ds.is_delete='N' ";

		}

		@Override
		public DiscountMasterData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String discountCode = resultSet.getString("discountCode");
			final String discountDescription = resultSet.getString("discountDescription");
			final String discountType = resultSet.getString("discountType");
			final BigDecimal discountRate = resultSet.getBigDecimal("discountRate");
			final LocalDate startDate = JdbcSupport.getLocalDate(resultSet, "startDate");
			final LocalDate endDate = JdbcSupport.getLocalDate(resultSet, "endDate");
			final String discountStatus = resultSet.getString("discountStatus");

			return new DiscountMasterData(id, discountCode, discountDescription, discountType, discountRate, startDate,endDate, discountStatus);

		}
	}

	/**
	 * (non-Java doc)
	 * 
	 * @see #retrieveSingleDiscountDetail(java.lang.Long)
	 */
	@Override
	public DiscountMasterData retrieveSingleDiscountDetail(Long discountId) {
		try {
			final DiscountMapper mapper = new DiscountMapper();
			final String sql = "select " + mapper.schema() + " and ds.id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { discountId });
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	@Override
	public List<DiscountDetailData> retrieveDiscountdetails(Long discountId) {
		try {
			final DiscountDetailsMapper mapper = new DiscountDetailsMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] { discountId });
		} catch (EmptyResultDataAccessException dve) {
			return null;
		}
	}

	private static final class DiscountDetailsMapper implements RowMapper<DiscountDetailData> {

		public String schema() {
			return "  dd.id AS id,if(dd.category_type = '0', 'Default', mcv.code_value) AS categoryType,dd.category_type AS categoryTypeId,"
					+ " dd.discount_rate AS discountRate"
					+ " FROM b_discount_details dd left join m_code_value mcv on mcv.id = dd.category_type"
					+ " WHERE dd.discount_id = ? AND  dd.is_deleted = 'N' group by dd.id";
		}

		@Override
		public DiscountDetailData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String categoryType = rs.getString("categoryType");
			final Long categoryTypeId = rs.getLong("categoryTypeId");
			final BigDecimal discountRate = rs.getBigDecimal("discountRate");

			return new DiscountDetailData(id, categoryType, categoryTypeId, discountRate);
		}

	}

}
