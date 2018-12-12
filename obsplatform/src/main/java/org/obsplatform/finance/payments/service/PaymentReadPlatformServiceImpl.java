package org.obsplatform.finance.payments.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.time.LocalDate;
import org.obsplatform.billing.invoice.data.InvoiceData;
import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.finance.payments.data.PaymentData;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PaymentReadPlatformServiceImpl implements PaymentReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PaymentReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class PaymodeMapper implements RowMapper<McodeData> {

		public String codeScheme() {
			return "b.id,code_value from m_code a, m_code_value b where a.id = b.code_id ";
		}
		
		@Override
		public McodeData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final Long id = rs.getLong("id");
			final String paymodeCode = rs.getString("code_value");

			return  McodeData.instance(id, paymodeCode);
		}

	}

	@Transactional
	@Override
	public Collection<McodeData> retrievemCodeDetails(final String codeName) {
		final PaymodeMapper mapper = new PaymodeMapper();
		final String sql = "select " + mapper.codeScheme()+" and code_name=?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { codeName });
	}

	@Override
	public McodeData retrieveSinglePaymode(final Long paymodeId) {
		final PaymodeMapper mapper = new PaymodeMapper();
		final String sql = "select " + mapper.codeScheme() + " and b.id="+ paymodeId;

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {});
	}

@Override
public McodeData retrievePaymodeCode(final JsonCommand command) {
	final PaymodeMapper1 mapper = new PaymodeMapper1();
	final String sql = "select id from m_code where code_name='" +command.stringValueOfParameterNamed("code_id")+"'";

	return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {});
}
private static final class PaymodeMapper1 implements RowMapper<McodeData> {

	@Override
	public McodeData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final Long id = rs.getLong("id");
		return  McodeData.instance1(id);
	}

}

@Override
public List<PaymentData> retrivePaymentsData(final Long clientId) {
	final String sql = "select (select display_name from m_client where id = p.client_id) as clientName, (select code_value from m_code_value where id = p.paymode_id) as payMode, p.payment_date as paymentDate, p.amount_paid as amountPaid, p.is_deleted as isDeleted, p.bill_id as billNumber, p.receipt_no as receiptNo from b_payments p where p.client_id=?";
	final PaymentsMapper pm = new PaymentsMapper();
 return jdbcTemplate.query(sql, pm,new Object[]{clientId});
}

private class PaymentsMapper implements RowMapper<PaymentData>{
	  @Override
	  public PaymentData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		  final String clientName = rs.getString("clientName");
		  final String payMode = rs.getString("payMode");
		  final LocalDate paymentDate = JdbcSupport.getLocalDate(rs, "paymentDate");
		  final BigDecimal amountPaid = rs.getBigDecimal("amountPaid");
		  final Boolean isDeleted = rs.getBoolean("isDeleted");
		  final Long billNumber = rs.getLong("billNumber");
		  final String receiptNumber = rs.getString("receiptNo");
	   return new PaymentData(clientName,payMode,paymentDate,amountPaid,isDeleted,billNumber,receiptNumber);
	  }
	 }

@Transactional
@Override
public Long getOnlinePaymode(String paymodeId) {
	try{
		    final Mapper mapper = new Mapper();
		    final String sql = "select id from m_code_value where code_value  LIKE '" + paymodeId + "'";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {});
			
	}catch (final EmptyResultDataAccessException e) {
		return null;
	}
}

private static final class Mapper implements RowMapper<Long> {
	
	@Override
	public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
		final Long id = rs.getLong("id");
		return id; 
	}

}

@Override
public List<PaymentData> retrieveClientPaymentDetails(final Long clientId) {

try{	
	final InvoiceMapper mapper = new InvoiceMapper();
	final String sql = "select " + mapper.schema();
	return this.jdbcTemplate.query(sql, mapper, new Object[] {clientId,clientId});
}catch(EmptyResultDataAccessException accessException){
	return null;
}

}

private static final class InvoiceMapper implements RowMapper<PaymentData> {

	public String schema() {
		/*return  "  * from (select p.id AS id,p.payment_date AS paymentdate,p.amount_paid AS amount,p.receipt_no AS recieptNo,"+
               " p.amount_paid-(ifnull((SELECT SUM(debit_amount) FROM b_deposit_refund b WHERE b.payment_id = p.id),0)) AS availAmount "+ 
               " FROM b_payments p join b_deposit_refund dr on p.client_id = dr.client_id and dr.payment_id = p.id "+
               " WHERE p.client_id =? AND p.invoice_id IS NULL GROUP BY p.id "+
               " UNION ALL "+
               " select p.id AS id,p.payment_date AS paymentdate,p.amount_paid AS amount,p.receipt_no AS recieptNo,"+
               " p.amount_paid-(ifnull((SELECT SUM(amount) FROM b_credit_distribution c WHERE c.payment_id = p.id),0)) AS availAmount "+ 
               " FROM b_payments p left join b_credit_distribution cd on p.client_id = cd.client_id "+
               " WHERE p.client_id =? AND p.invoice_id IS NULL GROUP BY p.id) as pay group by id ";
*/

         return     " * from (select p.id AS id, p.payment_date AS paymentdate, p.amount_paid AS amount, "+
               " p.receipt_no AS recieptNo, p.amount_paid - (ifnull((SELECT SUM(debit_amount) "+
               " FROM b_deposit_refund b WHERE b.payment_id = p.id), 0)) AS availAmount FROM "+
               " b_payments p join b_deposit_refund dr ON p.client_id = dr.client_id and dr.payment_id = p.id "+
               " WHERE p.client_id = ? AND p.invoice_id IS NULL GROUP BY p.id " +
               " UNION ALL select "+
               " p.id AS id, p.payment_date AS paymentdate, p.amount_paid AS amount, p.receipt_no AS recieptNo, "+
               " p.amount_paid - (ifnull((SELECT SUM(amount) FROM b_credit_distribution c WHERE c.payment_id = p.id), 0)) + (ifnull((SELECT  " +
               " SUM(a.amount_paid) AS CSum FROM b_payments a WHERE a.ref_id = p.id GROUP BY a.ref_id), 0)) AS availAmount FROM b_payments p "+
               " left join b_credit_distribution cd ON p.client_id = cd.client_id WHERE p.client_id = ? "+
               " AND p.invoice_id IS NULL and p.ref_id IS NULL) as pay group by id ";
	
	}


	@Override
	public PaymentData mapRow(final ResultSet rs,final int rowNum)
			throws SQLException {

		final Long id = rs.getLong("id");
		final LocalDate paymentdate=JdbcSupport.getLocalDate(rs,"paymentdate");
		final BigDecimal amount=rs.getBigDecimal("amount");
		final BigDecimal availAmount=rs.getBigDecimal("availAmount");
		final String  recieptNo=rs.getString("recieptNo");
		
		return new PaymentData(id,paymentdate,amount,recieptNo,availAmount);

	}
}

@Override
public List<PaymentData> retrieveDepositDetails(Long id) {

try{	
	context.authenticatedUser();
	DepositMapper mapper = new DepositMapper();

	String sql = "select " + mapper.schema() + " order by transactionDate desc limit 0,10";

	return this.jdbcTemplate.query(sql, mapper, new Object[] {id});
}catch(EmptyResultDataAccessException accessException){
	return null;
}

}

private static final class DepositMapper implements RowMapper<PaymentData> {

	public String schema() {
		return "bdr.id as id, bdr.transaction_date as transactionDate, bdr.debit_amount as debitAmount from b_deposit_refund bdr "+
				"where bdr.client_id = ? and transaction_type = 'Deposit' and bdr.payment_id is NULL";

	}


	@Override
	public PaymentData mapRow(final ResultSet rs,@SuppressWarnings("unused") final int rowNum)
			throws SQLException {

		Long id = rs.getLong("id");
		Date transactionDate=rs.getDate("transactionDate");
		BigDecimal debitAmount=rs.getBigDecimal("debitAmount");
		return new PaymentData(id, transactionDate, debitAmount);

	}
}
	public String retreieveNum(Long clientId){
		String referencenum="";
		if(clientId ==null){
			return referencenum;
		}else{
			int num = clientId.intValue();
			
			int uniqueId = (int) (System.currentTimeMillis() & 0xffff);
		 	System.out.println(uniqueId);
		 	String refnum = String.valueOf(num+uniqueId);
		 	referencenum="TC"+refnum;
			return referencenum;
		}
	}
	

}