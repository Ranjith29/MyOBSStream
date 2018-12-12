package org.obsplatform.finance.creditdistribution.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.creditdistribution.data.CreditDistributionData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditDistributionReadPlatformServiceImpl implements CreditDistributionReadPlatformService{
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<CreditDistributionData> paginationHelper = new PaginationHelper<CreditDistributionData>();
	@Autowired
	public CreditDistributionReadPlatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource){
		this.context=context;
		this.jdbcTemplate=new JdbcTemplate(dataSource);
		
	}

	@Transactional
	@Override
	public Page<CreditDistributionData> getClientDistributionData(Long clientId) {

		planServiceMapper mapper = new planServiceMapper();
		String sql = "select " + mapper.schema();

	//	return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sql,new Object[] {clientId,clientId}, mapper);

	}

	protected static final class planServiceMapper implements RowMapper<CreditDistributionData> {

		@Override
		public CreditDistributionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			Long id=rs.getLong("id");
			LocalDate distributionDate=JdbcSupport.getLocalDate(rs,"distributionDate");
			Long paymentId=rs.getLong("paymentId");
			Long invoiceId = rs.getLong("invoiceId");
			BigDecimal amount=rs.getBigDecimal("amount");

       
			return new CreditDistributionData(id,distributionDate,paymentId,invoiceId,amount);

		}


		public String schema() {
			return " cd.id as id,cd.distribution_date as distributionDate,cd.payment_id as paymentId,cd.invoice_id as invoiceId,cd.amount as amount" +
					" from b_credit_distribution cd where cd.client_id=? and cd.is_deleted = 'N' "+
					" union all "+
					" select dr.id as id,dr.lastmodified_date as distributionDate,dr.payment_id as paymentId,dr.id as invoiceId,dr.debit_amount as amount"+
                    " from b_deposit_refund dr where dr.client_id=? and dr.payment_id Is Not Null";
		}
		
		public String schema1() {
			return " cd.id as id,cd.distribution_date as distributionDate,cd.payment_id as paymentId,cd.invoice_id as invoiceId,cd.amount as amount" +
					" from b_credit_distribution cd where payment_id = ? and cd.is_deleted = 'N' limit 1";
		}
	}
	
	@Override
	public List<CreditDistributionData> retrievePaymentId(final Long paymentId) {
		
		planServiceMapper mapper = new planServiceMapper();
		String sql = "select " + mapper.schema1();
				
		return this.jdbcTemplate.query(sql, mapper, new Object[] { paymentId });
	}

	
}
