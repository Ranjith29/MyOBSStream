package org.obsplatform.billing.currency.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.obsplatform.billing.currency.data.CountryCurrencyData;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 * 
 */
@Service
public class CountryCurrencyReadPlatformServiceImpl implements CountryCurrencyReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public CountryCurrencyReadPlatformServiceImpl(final RoutingDataSource dataSource) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #getCountryCurrencyDetailsByName(java.lang.String)
	 */
	@Override
	public List<CountryCurrencyData> getCountryCurrencyDetailsByName(final String country) {

		try {
			final CurrencyMapper mapper = new CurrencyMapper();
			final String sql = "select " + mapper.schema() + " WHERE country = ? and  c.is_deleted='N' ";
			return this.jdbcTemplate.query(sql, mapper,new Object[] { country });
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class CurrencyMapper implements
			RowMapper<CountryCurrencyData> {

		public String schema() {
			return "  c.id as id,c.country as country,c.currency as currency,c.status as status,c.base_currency as baseCurrency, "
					+ "  c.conversion_rate as conversionRate, c.country_isd as countryISD  FROM b_country_currency c ";

		}

		@Override
		public CountryCurrencyData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String country = rs.getString("country");
			final String currency = rs.getString("currency");
			final String status = rs.getString("status");
			final String baseCurrency = rs.getString("baseCurrency");
			final BigDecimal conversionRate = rs.getBigDecimal("conversionRate");
			final String countryISD = rs.getString("countryISD");

			return new CountryCurrencyData(id, country, currency, baseCurrency,conversionRate, status, countryISD);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveAllCurrencyConfigurationDetails()
	 */
	@Override
	public Collection<CountryCurrencyData> retrieveAllCurrencyConfigurationDetails() {

		try {

			final CurrencyMapper mapper = new CurrencyMapper();
			final String sql = "select " + mapper.schema() + " WHERE  c.is_deleted='N' ";
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException exception) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveSingleCurrencyConfigurationDetails(java.lang.Long)
	 */
	@Override
	public CountryCurrencyData retrieveSingleCurrencyConfigurationDetails(final Long currencyId) {
		try {
			final CurrencyMapper mapper = new CurrencyMapper();
			final String sql = "select " + mapper.schema() + " WHERE  c.is_deleted='N' and c.id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { currencyId });
		} catch (EmptyResultDataAccessException exception) {
			return null;
		}
	}

}
