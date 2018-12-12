package org.obsplatform.portfolio.transactionhistory.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.transactionhistory.data.TransactionHistoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class TransactionHistoryReadPlatformServiceImp implements TransactionHistoryReadPlatformService{

	
	
	private JdbcTemplate jdbcTemplate;
	private PlatformSecurityContext context;
	private final PaginationHelper<TransactionHistoryData> paginationHelper = new PaginationHelper<TransactionHistoryData>();
	
	@Autowired
	public TransactionHistoryReadPlatformServiceImp(final RoutingDataSource dataSource,
			final PlatformSecurityContext context) {
		
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}

	public Page<TransactionHistoryData> retriveTransactionHistoryClientId(SearchSqlQuery searchTransactionHistory,final Long clientId) {
		
		return  retriveByClientId(searchTransactionHistory,clientId);
	}
	
	private Page<TransactionHistoryData> retriveByClientId(SearchSqlQuery searchTransactionHistory,Long id){
		try{
			
			context.authenticatedUser();
			ClientTransactionHistoryMapper rowMapper = new ClientTransactionHistoryMapper();
			String sql = "select "+rowMapper.schema()+" and  pcs.client_id = ? ";
			StringBuilder sqlBuilder = new StringBuilder(200);
		    sqlBuilder.append(sql);
		    String sqlSearch = searchTransactionHistory.getSqlSearch();
		    String extraCriteria = "";
		    if (sqlSearch != null) {
		    	sqlSearch=sqlSearch.trim();
		    	extraCriteria = "and (pcs.action_name like '%"+sqlSearch+"%' OR pcs.entity_name like '%"+sqlSearch+"%' OR" +
		    			" pcs.made_on_date like '%"+sqlSearch+"%' OR a.username like '%"+sqlSearch+"%' OR" +
		    		    " pcs.command_as_json like '%"+sqlSearch+"%')";
			    }
			   
		    sqlBuilder.append(extraCriteria);
		    if (searchTransactionHistory.isLimited()) {
		    	sqlBuilder.append(" order by transactionDate desc  limit ").append(searchTransactionHistory.getLimit());
		    }
		    if (searchTransactionHistory.isOffset()) {
		    	sqlBuilder.append(" offset ").append(searchTransactionHistory.getOffset());
		     }
		    return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
			            new Object[] {id}, rowMapper);
				
		}catch(DataIntegrityViolationException dve){
			throw new PlatformDataIntegrityException("", "", "");
		}
		
	}
	
	private class ClientTransactionHistoryMapper implements RowMapper<TransactionHistoryData>{
		
		public String schema(){
			
			return "SQL_CALC_FOUND_ROWS pcs.id AS id,pcs.client_id AS clientId,"+
				   "pcs.action_name AS actionName,pcs.entity_name AS entityName,pcs.made_on_date AS transactionDate,"+
				   "pcs.resource_id as resourceId,pcs.command_as_json AS history,a.username as userName "+
				   "FROM m_portfolio_command_source pcs,m_appuser a WHERE a.id = pcs.maker_id";
		}
		
		
		@Override
		public TransactionHistoryData mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long id = rs.getLong("id");
			Long clientId = rs.getLong("clientId");
			String transactionType = rs.getString("actionName")+" "+ rs.getString("entityName");
			DateTime transactionDate=JdbcSupport.getDateTime(rs,"transactionDate");
			String resourceId=rs.getString("resourceId");
			String history = rs.getString("history");
			String user=rs.getString("userName");
			return new TransactionHistoryData(id,clientId, transactionType, transactionDate,null, history,user);
		}

	}
	
private class ClientOldTransactionHistoryMapper implements RowMapper<TransactionHistoryData>{
		
	
		
		@Override
		public TransactionHistoryData mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long id = rs.getLong("id");
			Long clientId = rs.getLong("clientId");
			String transactionType = rs.getString("transactionType");
			DateTime transactionDate=JdbcSupport.getDateTime(rs,"transactionDate");
			String history = rs.getString("history");
			String user=rs.getString("userName");
			return new TransactionHistoryData(id,clientId, transactionType, transactionDate, null, history,user);
		}

	}
	
	private String query(){
		return " SQL_CALC_FOUND_ROWS th.id AS id,th.client_id AS clientId,th.transaction_type AS transactionType,th.transaction_date AS transactionDate,th.history AS history," +
		" a.username as userName FROM b_transaction_history th,m_appuser a WHERE a.id = th.createdby_id ";
		}

	@Override
	public Page<TransactionHistoryData> retriveTransactionHistoryById(SearchSqlQuery searchTransactionHistory, Long clientId) {context.authenticatedUser();
	
		try{
			String sql = "select "+query()+" and th.client_id = ? order by transactionDate desc ";
			ClientOldTransactionHistoryMapper rowMapper = new ClientOldTransactionHistoryMapper();
			StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder.append(sql);
			String sqlSearch = searchTransactionHistory.getSqlSearch();
			String extraCriteria = "";
			
			if (sqlSearch != null) {
				sqlSearch=sqlSearch.trim();
				extraCriteria = " and (th.transaction_type like '%"+sqlSearch+"%' OR "
						+ " th.transaction_date like '%"+sqlSearch+"%' OR "
						+ " a.username like '%"+sqlSearch+"%' OR "
						+ " th.history like '%"+sqlSearch+"%') " ;
			}
			sqlBuilder.append(extraCriteria);
			if (searchTransactionHistory.isLimited()) {
				sqlBuilder.append(" limit ").append(searchTransactionHistory.getLimit());
			}
			if (searchTransactionHistory.isOffset()) {
				sqlBuilder.append(" offset ").append(searchTransactionHistory.getOffset());
			}
	
	return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),new Object[] {clientId}, rowMapper);
	
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}
	}
}
