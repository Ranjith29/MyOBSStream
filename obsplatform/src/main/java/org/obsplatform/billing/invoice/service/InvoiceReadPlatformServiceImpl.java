package org.obsplatform.billing.invoice.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.invoice.data.InvoiceData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class InvoiceReadPlatformServiceImpl implements InvoiceReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public InvoiceReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {

		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<InvoiceData> retrieveInvoiceDetails(Long id) {

		try {
			context.authenticatedUser();
			InvoiceMapper mapper = new InvoiceMapper();
			String sql = "select " + mapper.schema() + " order by invoiceDate desc limit 0,10";
			return this.jdbcTemplate.query(sql, mapper, new Object[] { id });
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	private static final class InvoiceMapper implements RowMapper<InvoiceData> {

		public String schema() {
			return "bi.id as id,bi.invoice_date as invoiceDate,bi.invoice_amount as invoiceAmount,bi.due_amount as dueAmount,bi.bill_id as billId "
					+ " from b_invoice bi where bi.client_id=? and due_amount !=0  and bi.invoice_amount > 0 ";

		}

		@Override
		public InvoiceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Date invoiceDate = rs.getDate("invoiceDate");
			final BigDecimal invoiceAmount = rs.getBigDecimal("invoiceAmount");
			final BigDecimal dueAmount = rs.getBigDecimal("dueAmount");
			final Long billId = rs.getLong("billId");
			
			return new InvoiceData(id, invoiceAmount, dueAmount, invoiceDate,billId);

		}
	}

	@Override
	public List<InvoiceData> retrieveDueAmountInvoiceDetails(Long clientId) {

		try {
			context.authenticatedUser();
			InvoiceMapper mapper = new InvoiceMapper();
			String sql = "select " + mapper.schema() + " and due_amount !=0";
			return this.jdbcTemplate.query(sql, mapper,new Object[] { clientId });
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	@Override
	public List<InvoiceData> retrieveExpiryOrderList() {

		try {
			this.context.authenticatedUser();
			final ExpiryMapper mapper = new ExpiryMapper();
			String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class ExpiryMapper implements RowMapper<InvoiceData> {

		public String schema() {
			return " c.id as clientId,c.display_name as customerName,c.email as email,pm.plan_description as planName,"
					+ "  o.end_date as expiryDate,o.id as orderId from  m_client c join b_orders o ON c.id = o.client_id "
					+ " join b_plan_master pm on o.plan_id = pm.id where o.order_status = 1 and o.is_deleted <> 'y' "
					+ " and o.end_date = (DATE_FORMAT(date_add(now(), interval 25 day),'%Y-%m-%d')) group by o.id ";
		}

		@Override
		public InvoiceData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long clientId = rs.getLong("clientId");
			final String customerName = rs.getString("customerName");
			final String email = rs.getString("email");
			final Long orderId = rs.getLong("orderId");
			final String planName = rs.getString("planName");
			final LocalDate expiryDate = JdbcSupport.getLocalDate(rs,"expiryDate");

			return new InvoiceData(clientId, customerName, email, orderId,planName, expiryDate);

		}
	}

}
