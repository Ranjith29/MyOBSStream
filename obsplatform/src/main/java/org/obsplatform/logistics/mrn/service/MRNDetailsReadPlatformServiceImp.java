package org.obsplatform.logistics.mrn.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.mrn.data.InventoryTransactionHistoryData;
import org.obsplatform.logistics.mrn.data.MRNDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MRNDetailsReadPlatformServiceImp implements MRNDetailsReadPlatformService{

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
	private final PaginationHelper<MRNDetailsData> paginationHelper = new PaginationHelper<MRNDetailsData>();
	private final PaginationHelper<InventoryTransactionHistoryData> paginationHelper2 = new PaginationHelper<InventoryTransactionHistoryData>();
	
	@Autowired
	public MRNDetailsReadPlatformServiceImp(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	private final class MRNDetailsMapper implements RowMapper<MRNDetailsData>{
		@Override
		public MRNDetailsData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			final String id = rs.getString("mrnId");
			final LocalDate requestedDate =JdbcSupport.getLocalDate(rs,"requestedDate");
			final String fromOffice = rs.getString("fromOffice");
			final String toOffice = rs.getString("toOffice");
			final Long orderdQuantity = rs.getLong("orderdQuantity");
			final Long receivedQuantity = rs.getLong("receivedQuantity");
			final String status = rs.getString("status");
			final String itemDescription = rs.getString("item");
			
			return new MRNDetailsData(id, requestedDate, fromOffice, toOffice, orderdQuantity, receivedQuantity, status,itemDescription);
		}
	}
	
	private final class MRNDetailsMrnIDsMapper implements RowMapper<MRNDetailsData>{
		@Override
		public MRNDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {
			final Long mrnId = rs.getLong("mrnId");
			final Long itemMasterId = rs.getLong("itemMasterId");
			final String itemDescription = rs.getString("itemDescription");
			return new MRNDetailsData(mrnId,itemDescription,itemMasterId,null);
		}
	}
	
	private final class MRNDetailsSerialMapper implements RowMapper<String>{
		
		@Override
		public String mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			return 	rs.getString("serialNumber");
			
		}
	}
	
	private final class MRNDetailsHistoryMapper implements RowMapper<InventoryTransactionHistoryData>{
		@Override
		public InventoryTransactionHistoryData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			final String itemDescription = rs.getString("itemDescription");
			final String serialNumber = rs.getString("serialNumber");
			final LocalDate transactionDate =JdbcSupport.getLocalDate(rs,"transactionDate");
			final String fromOffice = rs.getString("source");
			final String toOffice = rs.getString("destination");
			final String refType = rs.getString("refType");
			final String movement = rs.getString("movement");
			return new InventoryTransactionHistoryData(transactionDate,itemDescription, fromOffice, toOffice,serialNumber,refType,movement);
		}
	}
	
	@Override
	public Page<MRNDetailsData> retriveMRNDetails(SearchSqlQuery searchMRNDetails) {
		this.context.authenticatedUser();
		final String sql = "Select Concat("+"'MRN ('"+",mrn.id,')') as mrnId, mrn.requested_date as requestedDate," +
				"(select item_description from b_item_master where id=mrn.item_master_id) as item," +
				" (select name from m_office where id=mrn.from_office) as fromOffice, (select name from m_office where id = mrn.to_office) as toOffice," +
				"mrn.orderd_quantity as orderdQuantity, mrn.received_quantity as receivedQuantity, mrn.status as status from b_mrn mrn ";
		
		MRNDetailsMapper rowMapper = new MRNDetailsMapper();
		StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append(sql).append(" where mrn.status = 'Completed' | 'New' | 'Pending' ");
        
         String sqlSearch = searchMRNDetails.getSqlSearch();
        String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch=sqlSearch.trim();
	    	extraCriteria = " and ((select item_description from b_item_master where id=mrn.item_master_id) like '%"+sqlSearch+"%' OR "
	    				+ " (select name from m_office where id=mrn.from_office) like '%"+sqlSearch+"%' OR "
	    				+ " (select name from m_office where id = mrn.to_office) like '%"+sqlSearch+"%' OR "
	    				+ " mrn.status like '%"+sqlSearch+"%')" ;
	    }
            sqlBuilder.append(extraCriteria);
            
            final String itemSql = "Union all Select Concat ("+"'Item Sale ' "+",its.id,')') as id,its.purchase_date as requestedDate," +
            		"(select item_description from b_item_master where id=its.item_id) as item,(select name from m_office where id=1) as " +
            		" fromOffice,(select name from m_office where id = its.purchase_by) as toOffice, its.order_quantity as orderdQuantity," +
            		"its.received_quantity as receivedQuantity, its.status as status  from b_itemsale its ";
           
            sqlBuilder.append(itemSql).append(" where its.status = 'Completed' | 'New' | 'Pending' ");
            String extraCriteriaForItemsale = "";
    	    if (sqlSearch != null) {
    	    	sqlSearch=sqlSearch.trim();
    	    	extraCriteriaForItemsale = "and ((select item_description from b_item_master where id=its.item_id ) like  '%"+sqlSearch+"%' OR" +
    	    			"(select name from m_office where id=its.purchase_by) like '%head%' OR its.status like  '%"+sqlSearch+"%') ";

    	    }
                sqlBuilder.append(extraCriteriaForItemsale).append("order by 2 desc");
        
      
        if (searchMRNDetails.isLimited()) {
            sqlBuilder.append(" limit ").append(searchMRNDetails.getLimit());
        }

        if (searchMRNDetails.isOffset()) {
            sqlBuilder.append(" offset ").append(searchMRNDetails.getOffset());
        }
		

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
	            new Object[] {}, rowMapper);
	}
	
	@Override
	public InventoryTransactionHistoryData retriveSingleMovedMrn(Long mrnId) {
		final String sql = "select id as id,ref_id as mrnId,ref_type as refType,(select item_description from b_item_master where id = item_master_id) as itemDescription, serial_number as serialNumber, transaction_date as transactionDate, 'From Office to To Office' movement,(select name from m_office where id = from_office) as source, (select name from m_office where id = to_office) as destination from b_item_history where id = ?";
		MRNDetailsHistoryMapper rowMapper = new MRNDetailsHistoryMapper();
		return jdbcTemplate.queryForObject(sql, rowMapper,new Object[]{mrnId});
	}
	
	
	@Override
	public List<MRNDetailsData> retriveMRNDetails() {
		final String sql = "select mrn.id as mrnId, mrn.requested_date as requestedDate, (select item_description from b_item_master where id=mrn.item_master_id) as item,(select name from m_office where id=mrn.from_office) as fromOffice, (select name from m_office where id = mrn.to_office) as toOffice, mrn.orderd_quantity as orderdQuantity, mrn.received_quantity as receivedQuantity, mrn.status as status from b_mrn mrn";
		MRNDetailsMapper rowMapper = new MRNDetailsMapper();
		return jdbcTemplate.query(sql, rowMapper);
	}

	
	@Override
	public Collection<MRNDetailsData> retriveMrnIds() {
		final String sql = "select id as mrnId,(select item_description from b_item_master where id=item_master_id) as itemDescription, item_master_id as itemMasterId from b_mrn where orderd_quantity>received_quantity order by requested_date desc";//"select id as mrnId,(select item_description from b_item_master where id=item_master_id) as itemDescription, item_master_id as itemMasterId from b_mrn order by requested_date desc";
		MRNDetailsMrnIDsMapper rowMapper = new MRNDetailsMrnIDsMapper();
		return jdbcTemplate.query(sql,rowMapper);
	}
	
	@Override
	public List<String> retriveSerialNumbers(final Long mrnId) {
		try{
		
			final String sql = "select idt.serial_no as serialNumber from b_mrn ots left join b_item_detail idt on idt.item_master_id = ots.item_master_id" +
				"  where ots.id = ? and idt.client_id is null and idt.office_id=ots.from_office";
		
				final MRNDetailsSerialMapper rowMapper = new MRNDetailsSerialMapper();
				return jdbcTemplate.query(sql,rowMapper,new Object[]{mrnId});
		
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}
	}
	
	
	
	
	
	@Override
	public Page<InventoryTransactionHistoryData> retriveHistory(SearchSqlQuery searchItemHistory) {
		
		final String sql = 
				
				"  SQL_CALC_FOUND_ROWS  * FROM ( " +
				" Select 'Item Detail' as refType, (SELECT item_description FROM b_item_master  " +
				" WHERE id = item_master_id) AS itemDescription, serial_no  AS serialNumber, created_date AS transactionDate," +
				" 'From Supplier to To Office' AS movement, (SELECT b.supplier_description FROM b_grn a, b_supplier b " +
				" WHERE a.supplier_id = b.id AND a.id = grn_id ) AS source, (SELECT name FROM m_office WHERE id = location_id) AS destination" +
				" from b_item_detail " +
				
				" UNION ALL " +
				" SELECT ref_type AS refType, (SELECT item_description FROM b_item_master WHERE id = item_master_id)" +
				" AS itemDescription,serial_number AS serialNumber,transaction_date AS transactionDate,'From Office to To Office' movement," +
				" (SELECT name FROM m_office WHERE id = from_office) AS source, (SELECT name FROM m_office WHERE id = to_office) AS destination" +
				" FROM b_item_history WHERE ref_type = 'MRN' " +
				
				" UNION ALL" +
				" SELECT ref_type AS refType, (SELECT item_description FROM b_item_master WHERE id = item_master_id) AS itemDescription, serial_number AS serialNumber," +
				" transaction_date AS transactionDate, 'From Office to To Client' movement, (SELECT name FROM m_office WHERE id = from_office) AS source," +
				" (SELECT concat(id, ' - ', display_name) FROM m_client WHERE id = to_office) AS destination " +
				" FROM b_item_history WHERE ref_type = 'Allocation'" +
				
				" UNION ALL " +
				" SELECT ref_type AS refType,(SELECT item_description FROM b_item_master WHERE id = item_master_id) AS itemDescription,serial_number AS serialNumber," +
				" transaction_date AS transactionDate, 'From Client to To Office' movement, (SELECT concat(id, ' - ', display_name) FROM m_client " +
				" WHERE id = from_office) AS source, (SELECT name FROM m_office WHERE id = to_office) AS destination " +
				" FROM b_item_history WHERE ref_type = 'De Allocation') a ";
		
		MRNDetailsHistoryMapper detailsHistoryMapper = new MRNDetailsHistoryMapper();
		
		final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ").append(sql);
        
        String sqlSearch = searchItemHistory.getSqlSearch();
        String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch=sqlSearch.trim();
	    	extraCriteria = " where serialNumber LIKE '%"+sqlSearch+"%' order by transactionDate";
	    }
            sqlBuilder.append(extraCriteria);
        if (searchItemHistory.isLimited()) {
            sqlBuilder.append(" limit ").append(searchItemHistory.getLimit());
        }

        if (searchItemHistory.isOffset()) {
            sqlBuilder.append(" offset ").append(searchItemHistory.getOffset());
        }

		return this.paginationHelper2.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
	            new Object[] {}, detailsHistoryMapper);
	}
	@Override
	public MRNDetailsData retriveSingleMrnDetail(final Long mrnId) {
		
		final String sql = "select mrn.id as mrnId, mrn.requested_date as requestedDate, (select item_description from b_item_master where id=mrn.item_master_id) as item," +
				          "  (select name from m_office where id=mrn.from_office) as fromOffice, (select name from m_office where id = mrn.to_office) as toOffice," +
				          "   mrn.orderd_quantity as orderdQuantity, mrn.received_quantity as receivedQuantity, mrn.status as status from b_mrn mrn where mrn.id=?";
		final MRNDetailsMapper rowMapper = new MRNDetailsMapper();
		return jdbcTemplate.queryForObject(sql,rowMapper,new Object[]{mrnId});
	}

	
	
	
	
	@Override
	public List<String> retriveSerialNumbersForItems(final Long itemsaleId,final String serialNumber) {
		try{
		 String sql = " select idt.serial_no as serialNumber from b_itemsale bi left join b_item_detail idt on" +
				           " idt.item_master_id = bi.item_id where bi.id = ? and idt.client_id is null and idt.office_id=bi.purchase_from";
		
		 if(serialNumber != null){
			  sql += " and idt.serial_no='"+serialNumber+"'";
		}
		final MRNDetailsSerialMapper rowMapper = new MRNDetailsSerialMapper();
		return jdbcTemplate.query(sql,rowMapper,new Object[]{ itemsaleId });

		}catch(EmptyResultDataAccessException ex){
			return null;
		}
	}

	//For Mrn Track History
	@Override
	public List<InventoryTransactionHistoryData> retriveMrnTrackDetails(Long mrnId) {

		final String sql = "SELECT  ref_type AS refType,(SELECT item_description FROM b_item_master WHERE id = item_master_id) AS itemDescription,"+
                           "serial_number AS serialNumber,transaction_date AS transactionDate,'From Office to To Office' movement,"+
                           "(SELECT name FROM m_office WHERE id = from_office) AS source,(SELECT name FROM m_office WHERE id = to_office) AS destination "+
                           "FROM b_item_history WHERE ref_type = 'MRN' and ref_id = ?";

		MRNDetailsTrackMapper rowMapper = new MRNDetailsTrackMapper();
		return jdbcTemplate.query(sql, rowMapper, new Object[]{mrnId});
	}
	
	private final class MRNDetailsTrackMapper implements RowMapper<InventoryTransactionHistoryData>{

		@Override
		public InventoryTransactionHistoryData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final String itemDescription = rs.getString("itemDescription");
			final String serialNumber = rs.getString("serialNumber");
			final LocalDate transactionDate =JdbcSupport.getLocalDate(rs,"transactionDate");
			final String fromOffice = rs.getString("source");
			final String toOffice = rs.getString("destination");
			final String refType = rs.getString("refType");
			final String movement = rs.getString("movement");
			return new InventoryTransactionHistoryData(transactionDate,itemDescription, fromOffice, toOffice,serialNumber,refType,movement);
		}
	}
	
}

