package org.obsplatform.organisation.feemaster.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.db.SQLBuilder;

@Service
public class FeeMasterReadplatformServiceImpl implements FeeMasterReadplatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
	@Autowired
	public FeeMasterReadplatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	

	@Override
	public FeeMasterData retrieveSingleFeeMasterDetails(Long id) {
		try {
			context.authenticatedUser();
			FeeMasterDataMapper mapper = new FeeMasterDataMapper();
			String sql;
				sql = "select " + mapper.schema()+" where fm.id=? and  fm.is_deleted='N'"; 
		
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class FeeMasterDataMapper implements RowMapper<FeeMasterData> {

			public String schema() {
				return " fm.id as id,fm.fee_code as feeCode,fm.fee_description as feeDescription,fm.transaction_type as transactionType,fm.charge_code as chargeCode," +
						" fm.default_fee_amount as defaultFeeAmount, fm.is_refundable as isRefundable, fm.enabled as enabled from b_fee_master fm";
				
			}
			@Override
			public FeeMasterData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
				final Long id = rs.getLong("id");
				final String feeCode = rs.getString("feeCode");
				final String feeDescription = rs.getString("feeDescription");
				final String transactionType = rs.getString("transactionType");
				final String chargeCode = rs.getString("chargeCode");
				final BigDecimal defaultFeeAmount = rs.getBigDecimal("defaultFeeAmount");
				final String isRefundable = rs.getString("isRefundable");
				final Boolean enabled = rs.getBoolean("enabled");
				return new FeeMasterData(id,feeCode,feeDescription,transactionType,chargeCode,defaultFeeAmount,isRefundable, enabled);
			
			
			}
		
			public String RegionWiseFeeDetailsSchema() {
		
				
				return " fm.id AS id,fm.fee_code AS feeCode,fm.fee_description as feeDescription,fm.is_refundable as isRefundable,fm.transaction_type as transactionType," +
						" fm.charge_code AS chargeCode,ifnull(truncate(fd.amount, 2),fm.default_fee_amount) AS defaultFeeAmount,fm.enabled as enabled " +
						" FROM b_fee_master fm  LEFT JOIN  b_client_address ca ON ca.client_id = ?  LEFT JOIN b_state s ON s.state_name = ca.state "+
					    " LEFT JOIN b_priceregion_detail pd ON (pd.state_id = ifnull((SELECT DISTINCT c.id FROM b_fee_detail a, b_priceregion_detail b," +
					    " b_state c,b_client_address d,b_fee_master m WHERE b.priceregion_id = a.region_id AND b.state_id = c.id" +
					    " AND a.region_id = b.priceregion_id AND d.state = c.state_name AND d.address_key = 'PRIMARY'  AND d.client_id = ? " +
					    " AND m.transaction_type = ? AND m.id = a.fee_id  AND a.is_deleted = 'N' AND m.is_deleted = 'N'), 0)  " +
					    " AND pd.country_id = ifnull((SELECT DISTINCT  c.id FROM b_fee_detail a,b_priceregion_detail b, b_country c," +
					    " b_state s, b_client_address d, b_fee_master m WHERE b.priceregion_id = a.region_id AND b.country_id = c.id" +
					    " AND c.country_name = d.country  AND d.address_key = 'PRIMARY' AND d.client_id = ? AND m.transaction_type = ?" +
					    " AND m.id = a.fee_id AND a.is_deleted = 'N' AND m.is_deleted = 'N'),0)) LEFT JOIN b_priceregion_master prm " +
					    " ON prm.id = pd.priceregion_id LEFT JOIN b_fee_detail fd ON (fd.fee_id = fm.id AND fd.region_id = prm.id AND fd.is_deleted = 'N' " +
					    " and fd.category_id = ? ) "+
					    " WHERE fm.transaction_type = ? AND fm.is_deleted = 'N' GROUP BY fm.id ";
			}
}

	@Override
	public List<FeeMasterData> retrieveRegionPrice(Long id) {
		
		try{
			final RetrieveFeedetailDataMapper mapper = new RetrieveFeedetailDataMapper();			
			final String sql = "select " + mapper.scheme() +"   WHERE fee_id = ? AND fd.is_deleted = 'N'";
	    	return this.jdbcTemplate.query(sql, mapper, new Object[] {id});
		
		}catch (final EmptyResultDataAccessException e) {
		    return null;
		}

	}
	
	private static final class RetrieveFeedetailDataMapper implements RowMapper<FeeMasterData> {

		public String scheme() {
			return " fd.id AS id, fd.fee_id AS feeId, fd.region_id AS regionId, fd.amount AS amount, prm.priceregion_code AS regionName, " +
					" fd.plan_id As planId, pm.plan_description AS planName, fd.contract_period AS contractPeriod, " +
					" fd.category_id AS categoryId, cv.code_value  AS categoryType " +
					" from b_fee_detail fd JOIN b_priceregion_master prm ON fd.region_id = prm.id " +
					" LEFT JOIN b_plan_master pm ON fd.plan_id = pm.id LEFT JOIN m_code_value cv ON cv.id = fd.category_id  ";

		}
		
		@Override
		public FeeMasterData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
			
			final Long id = resultSet.getLong("id");
			final Long feeId = resultSet.getLong("feeId");
			final Long regionId = resultSet.getLong("regionId");
			final String regionName = resultSet.getString("regionName");
			final BigDecimal amount = resultSet.getBigDecimal("amount");
			final Long planId   = resultSet.getLong("planId");
			final String planName = resultSet.getString("planName");
			final String contractPeriod = resultSet.getString("contractPeriod");
			final Long categoryId = resultSet.getLong("categoryId");
			final String categoryType = resultSet.getString("categoryType");
			
			return new FeeMasterData(id, feeId, regionId, regionName, amount,planId, planName, contractPeriod, categoryId, categoryType);
		}
	}

	@Override
	public Collection<FeeMasterData> retrieveAllData(final String transactionType) {
		
		try{
			final FeeMasterDataMapper mapper = new FeeMasterDataMapper();	
			StringBuilder sql = new StringBuilder();
			sql.append("select " + mapper.schema() +"  where is_deleted = 'N'");
			if(transactionType != null){
				sql.append(" and fm.transaction_type ='"+transactionType+"'");
			}
	    	return this.jdbcTemplate.query(sql.toString(), mapper, new Object[] {});
		
		}catch (final EmptyResultDataAccessException e) {
		    return null;
		}
	}


	/* (non-Javadoc)
	 * #retrieveCustomerRegionWiseFeeDetails(java.lang.Long, java.lang.String)
	 */
	@Override
	public FeeMasterData retrieveCustomerRegionClientTypeWiseFeeDetails(final Long clientId,final String RegistrationFee, final Long categoryId) {

		try {
			final FeeMasterDataMapper mapper = new FeeMasterDataMapper();
			String sql = "select " + mapper.RegionWiseFeeDetailsSchema();
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] {clientId, clientId, RegistrationFee,
					clientId, RegistrationFee, categoryId, RegistrationFee});

		} catch (final EmptyResultDataAccessException e) {
			return null;
		}
	}

}
