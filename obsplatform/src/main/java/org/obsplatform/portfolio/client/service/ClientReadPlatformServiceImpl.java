/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.obsplatform.accounting.closure.data.LoanStatusEnumData;
import org.obsplatform.infrastructure.codes.data.CodeValueData;
import org.obsplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiParameterHelper;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.obsplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.obsplatform.portfolio.client.data.ClientAccountSummaryData;
import org.obsplatform.portfolio.client.data.ClientAdditionalData;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.domain.ClientEnumerations;
import org.obsplatform.portfolio.client.domain.ClientStatus;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.group.data.GroupGeneralData;
import org.obsplatform.portfolio.group.service.SearchParameters;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;

    // data mappers
    private final PaginationHelper<ClientData> paginationHelper = new PaginationHelper<ClientData>();
    //private final ClientMapper clientMapper = new ClientMapper();
    private final ClientLookupMapper lookupMapper = new ClientLookupMapper();
    private final ClientMembersOfGroupMapper membersOfGroupMapper = new ClientMembersOfGroupMapper();
    private final ParentGroupsMapper clientGroupsMapper = new ParentGroupsMapper();
	private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService,final CodeValueReadPlatformService codeValueReadPlatformService) {

    	this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.codeValueReadPlatformService=codeValueReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public PlatformSecurityContext getContext() {
        return this.context;
    }

    @Override
    public ClientData retrieveTemplate() {

        final AppUser currentUser = context.authenticatedUser();

        final Collection<OfficeData> offices = officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<ClientCategoryData> categoryDatas=this.retrieveClientCategories();
        final Collection<GroupData> groupDatas = this.retrieveGroupData();

        final Long officeId = currentUser.getOffice().getId();

        return ClientData.template(officeId, DateUtils.getLocalDateOfTenant(), offices,categoryDatas, groupDatas,null);
    }

    @Override
   // @Cacheable(value = "clients", key = "T(org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy())")
    public Page<ClientData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = context.authenticatedUser();
        final ClientMapper clientMapper = new ClientMapper();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        
        
        final String hierarchySearchString = hierarchy + "%";
       
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        
        sqlBuilder.append(clientMapper.schema());
        sqlBuilder.append(" where a.is_deleted='N' and a.address_key in ('PRIMARY','BILLING') and o.hierarchy like ?");

        final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        // DONT order by default - just use database natural ordering so doesnt
        // have to scan entire database table.
        // sql += " order by c.display_name ASC, c.account_no ASC";
        sqlBuilder.append(" group by c.id ");
        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
        }
        
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
        }

        if (searchParameters.isOffset()) {
            sqlBuilder.append(" offset ").append(searchParameters.getOffset());
        }
         
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                new Object[] { hierarchySearchString }, clientMapper);
    }
    
    private String buildSqlStringFromClientCriteria(final SearchParameters searchParameters) {

        final String sqlSearch = searchParameters.getSqlSearch();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String displayName = searchParameters.getName();
        final String firstname = searchParameters.getFirstname();
        final String lastname = searchParameters.getLastname();
        final String hierarchy = searchParameters.getHierarchy();
        final String groupName = searchParameters.getGroupName();
        final String status=searchParameters.getStatus();
        final Long categoryId= searchParameters.getCategorytype();

        String extraCriteria = "";
        if (sqlSearch != null) {
          //  extraCriteria = " and (" + sqlSearch + ")";
            
        	extraCriteria = " and ( display_name like '%" + sqlSearch + "%' OR c.phone like '%" + sqlSearch + "%' OR c.account_no like '%"+sqlSearch+"%' OR g.group_name like '%"+sqlSearch+"%' "
        			+" OR a.address_no like '%"+sqlSearch+"%' OR c.email like '%"+sqlSearch+"%'"
        			+ " OR IFNULL(( Select min(serial_no) from b_allocation ba where c.id=ba.client_id and ba. is_deleted = 'N'),'No Hardware') LIKE '%"+sqlSearch+"%' )";
            
        }

        if (officeId != null) {
            extraCriteria += " and office_id = " + officeId;
        }

        if (externalId != null) {
            extraCriteria += " and external_id like " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        if (displayName != null) {
            extraCriteria += " and concat(ifnull(firstname, ''), if(firstname > '',' ', '') , ifnull(lastname, '')) like "
                    + ApiParameterHelper.sqlEncodeString(displayName);
        }

        if (firstname != null) {
            extraCriteria += " and firstname like " + ApiParameterHelper.sqlEncodeString(firstname);
        }

        if (lastname != null) {
            extraCriteria += " and lastname like " + ApiParameterHelper.sqlEncodeString(lastname);
        }

        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }
        
        if (groupName != null) {
            extraCriteria += " and g.group_name = " + ApiParameterHelper.sqlEncodeString(groupName);
        }
        
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        
        if(status !=null){
        	final Integer statusValue=ClientStatus.fromStatus(status); 
        	extraCriteria += " c.status_enum like " + statusValue;
        }
        
        if (categoryId != null) {
            extraCriteria += " c.category_type = " + categoryId;
        }

        return extraCriteria;
    }

    @Override
    public ClientData retrieveOne(final Long clientId) {

        try {
        	final AppUser currentUser = context.authenticatedUser();
        	final String hierarchy = currentUser.getOffice().getHierarchy();
        	final String hierarchySearchString = hierarchy + "%";
            final ClientMapper clientMapper = new ClientMapper();
            final String sql = "select " + clientMapper.schema() + " where o.hierarchy like ? and c.id = ? and a.address_key='PRIMARY' group by c.id";
            final ClientData clientData = this.jdbcTemplate.queryForObject(sql,clientMapper,new Object[] { hierarchySearchString, clientId });
            final String clientGroupsSql = "select " + this.clientGroupsMapper.parentGroupsSchema();

            final Collection<GroupGeneralData> parentGroups = this.jdbcTemplate.query(clientGroupsSql, this.clientGroupsMapper,new Object[] { clientId });
            return ClientData.setParentGroups(clientData, parentGroups);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientData> retrieveAllForLookup(final String extraCriteria) {

        String sql = "select " + this.lookupMapper.schema();

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] {});
    }

    @Override
    public Collection<ClientData> retrieveAllForLookupByOfficeId(final Long officeId) {

        final String sql = "select " + this.lookupMapper.schema() + " and c.office_id = ?";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { officeId });
    }

    @Override
    public Collection<ClientData> retrieveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema()
 + " where o.hierarchy like ? and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, new Object[] { hierarchySearchString, groupId });
    }

    private static final class ClientMembersOfGroupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientMembersOfGroupMapper() { 
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("c.id as id,c.title as title, c.account_no as accountNo,g.group_name as groupName, c.external_id as externalId, ");
            sqlBuilder.append("c.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname,c.is_indororp as entryType, ");
            sqlBuilder.append("c.fullname as fullname, c.display_name as displayName,c.category_type as categoryType, ");
            sqlBuilder.append("c.email as email,c.phone as phone,c.home_phone_number as homePhoneNumber,c.activation_date as activationDate, c.image_key as imagekey,c.exempt_tax as taxExemption ");
            sqlBuilder.append("from m_client c ");
            sqlBuilder.append("join m_office o on o.id = c.office_id ");
            sqlBuilder.append("join m_group_client pgc on pgc.client_id = c.id");
            sqlBuilder.append(" left outer join b_group g on  g.id = c.group_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");
            final String groupName = rs.getString("groupName");
            final EnumOptionData status = null;
            final String title = rs.getString("title");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final String imageKey = rs.getString("imageKey");
            final String officeName = rs.getString("officeName");
            final String categoryType=rs.getString("categoryType");
            final String email = rs.getString("email");
            final String phone = rs.getString("phone");
            final String homePhoneNumber = rs.getString("homePhoneNumber");
            final String currency = rs.getString("currency");
            final String taxExemption = rs.getString("taxExemption");
            final String entryType=rs.getString("entryType");
            return ClientData.instance(accountNo,groupName, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                    externalId, activationDate, imageKey,categoryType,email,phone,homePhoneNumber, null, null,null, null,null,null, null,null,currency,taxExemption,
                    entryType,null,null,null,null,title,null);
        }
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        private final String schema;
        
        public ClientMapper() {
        	
        	final StringBuilder builder = new StringBuilder(400);

            builder.append(" c.id as id,c.title as title, c.account_no as accountNo,g.group_name as groupName, c.external_id as externalId, c.status_enum as statusEnum, ");
            builder.append(" c.office_id as officeId, o.name as officeName, c.registration_fee as registrationFee,");
            builder.append(" c.firstname as firstname, c.middlename as middlename, c.lastname as lastname,c.is_indororp as entryType, ");
            builder.append(" c.fullname as fullname, c.display_name as displayName,mc.code_value as categoryType, ");
            builder.append(" c.email as email,c.phone as phone,c.home_phone_number as homePhoneNumber,c.activation_date as activationDate, c.image_key as imagekey,c.exempt_tax as taxExemption, ");
            builder.append(" c.parent_id as parentId,cu.username as userName,cu.password as clientpassword, ");
            builder.append(" a.address_no as addrNo,a.street as street,a.city as city,a.state as state,a.country as country, ");
            builder.append(" a.zip as zipcode,b.balance_amount as balanceAmount,b.wallet_amount as walletAmount,bc.currency as currency,");
            builder.append(" coalesce(min(ba.serial_no),min(oh.serial_number),'No Device') HW_Serial ");
            builder.append(" from m_client c ");
            builder.append(" join m_office o on o.id = c.office_id ");
            builder.append(" left outer join b_client_balance b on  b.client_id = c.id ");
            builder.append(" left outer join b_group g on  g.id = c.group_id ");
            builder.append(" left outer join  m_code_value mc on  mc.id =c.category_type  ");
            builder.append(" left outer join b_client_address a on  a.client_id = c.id and is_deleted= 'n'");
            builder.append(" left outer join b_country_currency bc on  bc.country = a.country ");
            builder.append(" left outer join b_allocation ba on (c.id = ba.client_id AND ba.is_deleted = 'N')");
            builder.append(" left outer join b_owned_hardware oh on (c.id=oh.client_id  AND oh.is_deleted = 'N')");
            builder.append(" left outer join b_clientuser cu on cu.client_id = c.id ");
            this.schema = builder.toString();
        }

       

		public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");
            final String title = rs.getString("title");
            final String groupName = rs.getString("groupName");
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final String imageKey = rs.getString("imageKey");
            final String officeName = rs.getString("officeName");
            final String categoryType=rs.getString("categoryType");
            final String email = rs.getString("email");
            final String phone = rs.getString("phone");
            final String homePhoneNumber = rs.getString("homePhoneNumber");
            final String addressNo = rs.getString("addrNo");
            final String street = rs.getString("street");
            final String city = rs.getString("city");
            final String state = rs.getString("state");
            final String country = rs.getString("country");
            final String zipcode = rs.getString("zipcode");
            final String hwSerial = rs.getString("HW_Serial");
            final BigDecimal clientBalance = rs.getBigDecimal("balanceAmount");
            final BigDecimal walletAmount = rs.getBigDecimal("walletAmount");
            final String currency=rs.getString("currency");
            final String taxExemption=rs.getString("taxExemption");
            final String entryType=rs.getString("entryType");
            final String userName = rs.getString("userName");
            final String clientpassword = rs.getString("clientpassword");
            final String parentId = rs.getString("parentId");
            final String registrationFee = rs.getString("registrationFee");
           

            return ClientData.instance(accountNo,groupName, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                    externalId, activationDate, imageKey,categoryType,email,phone,homePhoneNumber, addressNo, street, city, state, country, zipcode,
                    clientBalance,hwSerial,currency,taxExemption,entryType,walletAmount,userName,clientpassword,parentId,title,registrationFee);
        }
    }

    private static final class ParentGroupsMapper implements RowMapper<GroupGeneralData> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.display_name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ?";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");

            return GroupGeneralData.lookup(groupId, groupName);
        }
    }

    private static final class ClientLookupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientLookupMapper() {
        	final StringBuilder builder = new StringBuilder(200);

            builder.append("c.id as id, c.display_name as displayName, ");
            builder.append("c.office_id as officeId, o.name as officeName ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.lookup(id, displayName, officeId, officeName);
        }
    }

    @Override
    public ClientAccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId) {

        try {
            this.context.authenticatedUser();

            // Check if client exists
            retrieveOne(clientId);

            final List<ClientAccountSummaryData> pendingApprovalLoans = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> awaitingDisbursalLoans = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> openLoans = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> closedLoans = new ArrayList<ClientAccountSummaryData>();

            final ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

            final String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ?";

            final List<ClientAccountSummaryData> results = this.jdbcTemplate.query(sql, rm, new Object[] { clientId });
            if (results != null) {
                for (ClientAccountSummaryData row : results) {

                	final LoanStatusMapper statusMapper = new LoanStatusMapper(row.accountStatusId());

                    if (statusMapper.isOpen()) {
                        openLoans.add(row);
                    } else if (statusMapper.isAwaitingDisbursal()) {
                        awaitingDisbursalLoans.add(row);
                    } else if (statusMapper.isPendingApproval()) {
                        pendingApprovalLoans.add(row);
                    } else {
                        closedLoans.add(row);
                    }
                }
            }

            final List<ClientAccountSummaryData> pendingApprovalDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> approvedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> withdrawnByClientDespositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> closedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> rejectedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> preclosedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> maturedDepositAccounts = new ArrayList<ClientAccountSummaryData>();

            final List<ClientAccountSummaryData> pendingApprovalSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> approvedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> withdrawnByClientSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> rejectedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            final List<ClientAccountSummaryData> closedSavingAccounts = new ArrayList<ClientAccountSummaryData>();

            final ClientSavingsAccountSummaryDataMapper savingsAccountSummaryDataMapper = new ClientSavingsAccountSummaryDataMapper();
            final String savingsSql = "select " + savingsAccountSummaryDataMapper.schema() + " where sa.client_id = ?";
            final List<ClientAccountSummaryData> savingsAccounts = this.jdbcTemplate.query(savingsSql, savingsAccountSummaryDataMapper,
                    new Object[] { clientId });

            approvedSavingAccounts.addAll(savingsAccounts);

            return new ClientAccountSummaryCollectionData(pendingApprovalLoans, awaitingDisbursalLoans, openLoans, closedLoans,
                    pendingApprovalDepositAccounts, approvedDepositAccounts, withdrawnByClientDespositAccounts, rejectedDepositAccounts,
                    closedDepositAccounts, preclosedDepositAccounts, maturedDepositAccounts, pendingApprovalSavingAccounts,
                    approvedSavingAccounts, withdrawnByClientSavingAccounts, rejectedSavingAccounts, closedSavingAccounts);

        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(final Long clientId, final Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if client exists
        retrieveOne(clientId);

        final ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

        final String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ? and l.loan_officer_id = ?";

        final List<ClientAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { clientId, loanOfficerId });

        return loanAccounts;
    }

    private static final class ClientSavingsAccountSummaryDataMapper implements RowMapper<ClientAccountSummaryData> {

        final String schemaSql;

        public ClientSavingsAccountSummaryDataMapper() {
        	final StringBuilder accountsSummary = new StringBuilder();
            accountsSummary.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId,");
            accountsSummary.append("sa.product_id as productId, p.name as productName ");
            accountsSummary.append("from m_savings_account sa ");
            accountsSummary.append("join m_savings_product as p on p.id = sa.product_id ");

            this.schemaSql = accountsSummary.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final LoanStatusEnumData loanStatus = null;

            return new ClientAccountSummaryData(id, accountNo, externalId, productId, loanProductName, loanStatus);
        }
    }

    private static final class ClientLoanAccountSummaryDataMapper implements RowMapper<ClientAccountSummaryData> {

        public String loanAccountSummarySchema() {

        	final StringBuilder accountsSummary = new StringBuilder("l.id as id, l.account_no as accountNo, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId ")
                    .append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            
            return new ClientAccountSummaryData(id, accountNo, externalId, productId, loanProductName, null);
        }
    }

    @Override
    public ClientData retrieveClientByIdentifier(final Long identifierTypeId, final String identifierKey) {
        try {
            final ClientIdentifierMapper mapper = new ClientIdentifierMapper();

            final String sql = "select " + mapper.clientLookupByIdentifierSchema();

            return jdbcTemplate.queryForObject(sql, mapper, new Object[] { identifierTypeId, identifierKey });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class ClientIdentifierMapper implements RowMapper<ClientData> {

        public String clientLookupByIdentifierSchema() {
            return "c.id as id, c.account_no as accountNo, c.status_enum as statusEnum, c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName,"
                    + "c.office_id as officeId, o.name as officeName "
                    + " from m_client c, m_office o, m_client_identifier ci "
                    + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, accountNo, status, firstname, middlename, lastname, fullname, displayName, officeId,
                    officeName);
        }
    }

	@Override
	public Collection<ClientCategoryData> retrieveClientCategories() {
		 try {
	            final ClientCategoryMapper mapper = new ClientCategoryMapper();

	            final String sql = "select " + mapper.clientLookupByCategorySchema() + " order by order_position asc ";

	            return jdbcTemplate.query(sql, mapper, new Object[] { });
	        } catch (EmptyResultDataAccessException e) {
	            return null;
	        }
	    }
	
	@Override
	public ClientCategoryData retrieveClientCategory(Long clientCategoryId) {
		 try {
	            final ClientCategoryMapper mapper = new ClientCategoryMapper();

	            final String sql = "select " + mapper.clientLookupByCategorySchema()  + " and mcv.id = ? order by order_position asc ";

	            return jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientCategoryId});
	        } catch (EmptyResultDataAccessException e) {
	            return null;
	        }
	    }

	    private static final class ClientCategoryMapper implements RowMapper<ClientCategoryData> {

	        public String clientLookupByCategorySchema() {
	            return " mcv.id AS id, mcv.code_value AS codeValue FROM m_code_value mcv,m_code mc  where mcv.code_id=mc.id"
	            		+" and mc.code_name='Client Category' ";
	        }

	        @Override
	        public ClientCategoryData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

	            final Long id = rs.getLong("id");
	            final String codeValue = rs.getString("codeValue");
                    return new ClientCategoryData(id,codeValue,null,null,null,null,null);
	            
	        }
	    }
	    @Override
	    public Collection<GroupData> retrieveGroupData(){
	    	try{
	    		
	    		final GroupDataMapper mapper = new GroupDataMapper();
	    		
	    		final String sql = "select "+mapper.groupDataSchema();
	    		
	    		return jdbcTemplate.query(sql,mapper,new Object[]{});
	    		
	    	}catch(EmptyResultDataAccessException e){
	    		return null;
	    	}
	    }
	    private static final class GroupDataMapper implements RowMapper<GroupData>{
	    	
	    	public String groupDataSchema(){
	    		
	    		return "bg.id as id,bg.group_name as groupName from b_group bg";
	    	}
	    	
	    	@Override
	    	public GroupData mapRow(final ResultSet rs,final int rowNum)throws SQLException{
	    		
	    		final Long id = rs.getLong("id");
	    		final String groupName = rs.getString("groupName");
	    		
	    		return new GroupData(id,groupName);
	    	}
}
	    
            private static final class AdditionalClientDataMapper implements RowMapper<ClientAdditionalData>{
	    	public String schema(){
	    		return "  a.id as id,a.client_id as clientId,a.job_title as jobTitle,a.gender_id as genderId,a.finance_id as financeId," +
	    			   "  a.uts_customer_id as utsCustomerId,a.date_of_birth as dob,a.nationality_id as nationalityId,a.age_group_id as ageGroupId," +
	    			   "  a.id_type as customerIdType,a.id_number as customerIdentification,a.prefere_lan_id as preferLanId," +
	    			   "  a.prefere_communication_id as preferCommId,a.remarks as remarks" +
	    			   "  FROM additional_client_fields a where client_id=?";
	    	}
	    	
	    	@Override
	    	public ClientAdditionalData mapRow(final ResultSet rs,final int rowNum)throws SQLException{
	    		
	    		final Long id = rs.getLong("id");
	    		final Long clientId = rs.getLong("clientId");
	    		final String financeId = rs.getString("financeId");
	    		final String utsCustomerId = rs.getString("utsCustomerId");
	    		final String customerIdentification = rs.getString("customerIdentification");
	    		final String jobTitle = rs.getString("jobTitle");
	    		final LocalDate dob = JdbcSupport.getLocalDate(rs,"dob");
	    		final Long nationalityId = rs.getLong("nationalityId");
	    		final Long ageGroupId = rs.getLong("ageGroupId");
	    		final Long customerIdType = rs.getLong("customerIdType");
	    		final Long preferCommId = rs.getLong("preferCommId");
	    		final Long preferLanId = rs.getLong("preferLanId");
	    		final Long genderId = rs.getLong("genderId");
	    		final String remarks = rs.getString("remarks");
	    		return new ClientAdditionalData(id,clientId,financeId,utsCustomerId,customerIdentification,jobTitle,dob,nationalityId,ageGroupId,
	    				customerIdType,preferCommId,preferLanId,genderId,remarks);
	    	}
           }
	    @Override
	    public ClientData retrieveAllClosureReasons(final String clientClosureReason) {
	        final List<CodeValueData> closureReasons = new ArrayList<CodeValueData>(
	                this.codeValueReadPlatformService.retrieveCodeValuesByCode(clientClosureReason));
	        return ClientData.template(null, null, null, null, closureReasons);
	    }
	    
	    
	    /* (non-Javadoc)
	     * @see #retrieveClientBillModes(java.lang.Long)
	     */
	    @Override
		public ClientCategoryData retrieveClientBillModes(final Long clientId) {
			
			try{

				this.context.authenticatedUser();
				final BillModeMapper mapper=new BillModeMapper();
				final String sql="select id as id , bill_mode as billMode from m_client where id=?";
				return this.jdbcTemplate.queryForObject(sql, mapper,new Object[]{clientId});
				
			  }catch(EmptyResultDataAccessException e){
				return null;
			}
		}
		
		private static final class BillModeMapper implements RowMapper<ClientCategoryData> {
		
		  @Override
	      public ClientCategoryData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
	          final Long id = rs.getLong("id");
	          final String billMode = rs.getString("billMode");
	         return new  ClientCategoryData(id,null, billMode,null,null,null,null);
	          
	      }
	}

		/* (non-Javadoc)
		 * @see #retrievingParentClients(java.lang.String)
		 */
		@Override
		public List<ClientCategoryData> retrievingParentClients(final String query) {
			
			try{
				this.context.authenticatedUser();
				final parentClientMapper mapper=new parentClientMapper();
				final String sql="select id as id , account_no as accountNo,display_name as displayName from m_client where parent_id is null" 
			 			         + " and display_name like '%"+query+"%' ORDER BY id LIMIT 20 ";
				return this.jdbcTemplate.query(sql, mapper,new Object[]{});
			}catch(EmptyResultDataAccessException e){
			return null;
		  }
		}
		
		/* (non-Javadoc)
		 * @see #retrievedParentAndChildData(java.lang.Long, java.lang.Long)
		 */
		@Override

		public List<ClientCategoryData> retrievedParentAndChildData(final Long parentClientId,final Long clientId) {
			
			   try{
				   this.context.authenticatedUser();
				  final parentClientMapper mapper=new parentClientMapper();
				  //check parentClient information
				   if(parentClientId !=null){
					   final String sql=mapper.parentChildSchema()+" where m.id= ? ";
					   return this.jdbcTemplate.query(sql, mapper,new Object[]{parentClientId});
				   }else{
					   final String sql=mapper.parentChildSchema()+" where m.parent_id= ?  ";
					   return this.jdbcTemplate.query(sql, mapper,new Object[]{clientId}); 
				   }
			  }catch(EmptyResultDataAccessException e){
			return null;
		  }
		}
		
       private static final class parentClientMapper implements RowMapper<ClientCategoryData> {
			
			public String parentChildSchema(){
	    		return "select id,account_no as accountNo,display_name as displayName from m_client m ";
	    	}
			  @Override
		      public ClientCategoryData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
		          final Long id = rs.getLong("id");
		          final String  accountNo = rs.getString("accountNo");
		          final String displayName = rs.getString("displayName");
		         return new  ClientCategoryData(id,null,null,accountNo, displayName,null,null);
		          
		      }
         }
		
		@Override
		public Boolean countChildClients(final Long entityId) {
			 context.authenticatedUser();
			 boolean result = false;
			 final String sql="select count(id) from m_client m where m.parent_id= ? ";
			 final int count=	this.jdbcTemplate.queryForObject(sql, Integer.class,new Object[]{entityId});
			 if(count > 0){
				 result = true;
			 }
			 return result;
			 
		}

		@Override
		public ClientAdditionalData retrieveClientAdditionalData(Long clientId) {
	    	try{
	    		
	    		final AdditionalClientDataMapper mapper = new AdditionalClientDataMapper();
	    		final String sql = "select "+mapper.schema();
	    		return  jdbcTemplate.queryForObject(sql,mapper,new Object[]{clientId});
	    	}catch(EmptyResultDataAccessException e){
	    		return null;
	    	}
	    }
		
		 private static final class ClientWalletMapper implements RowMapper<ClientData>{
		    	
		    	public String schema(){
		    		
		    		return " c.id as id, c.account_no as accountNo, b.wallet_amount as walletAmount, " +
		    				" coalesce(min(ba.serial_no),min(oh.serial_number),'No Device') HW_Serial from m_client c " +
		    				" left outer join b_client_balance b ON b.client_id = c.id " +
		    				" left outer join b_allocation ba on (c.id = ba.client_id AND ba.is_deleted = 'N') " +
		    				" left outer join b_owned_hardware oh on (c.id=oh.client_id  AND oh.is_deleted = 'N') "+
		    				" left outer join b_clientuser cu ON cu.client_id = c.id ";
		    	}
		    	
		    	@Override
		    	public ClientData mapRow(final ResultSet rs,final int rowNum)throws SQLException{
		    		
		    		final Long id = rs.getLong("id");
		    		final String  accountNo = rs.getString("accountNo");
		    		final BigDecimal walletAmount = rs.getBigDecimal("walletAmount");
		    		final String hwSerialNumber = rs.getString("HW_Serial");
		    		
		    		return ClientData.walletAmount(id, accountNo, walletAmount,hwSerialNumber);
		    	}
	}
		
		@Override
		public ClientData retrieveClientWalletAmount(Long clientId,String type) {
			try{
				
				final ClientWalletMapper mapper = new ClientWalletMapper();
				 String sql = "select "+mapper.schema();
				if(type != null && type.equalsIgnoreCase("userId")){
						sql = sql+" where cu.zebra_subscriber_id = ? ";
				}else{
					sql = sql+" where  c.id = ? ";
				}
				sql += " and c.status_enum <> 400 group by c.id ";
				
				return  jdbcTemplate.queryForObject(sql,mapper,new Object[]{clientId});
			}catch(EmptyResultDataAccessException e){
				return null;
			}
		}
		
		@Override
		public Collection<ClientData> retrieveConcvertedClients(Long clientId) {
			 try {
		            final ConvertedClientMapper mapper = new ConvertedClientMapper();

		            final String sql = "select " + mapper.convertedClientSchema();

		            return jdbcTemplate.query(sql, mapper, new Object[] {clientId});
		        } catch (EmptyResultDataAccessException e) {
		            return null;
		        }
		    }

		    private static final class ConvertedClientMapper implements RowMapper<ClientData> {

		        public String convertedClientSchema() {
		            return " c.id as id,c.firstname as firstName,c.email as email,ca.address_no as address, ca.city as city, " +
		            		" ca.state as state, ca.country as country from m_client c " +
		            		" left join b_client_address ca on (c.id=ca.client_id and ca.is_deleted = 'n') " +
		            		" where c.id = ? ";
		        }

		        @Override
		        public ClientData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

		            final Long id = rs.getLong("id");
		            final String firstName = rs.getString("firstName");
		            final String email = rs.getString("email");
		            final String address = rs.getString("address");
		            final String city = rs.getString("city");
		            final String state = rs.getString("state");
		            final String country = rs.getString("country");
	                    return ClientData.convertedClient(id,firstName,email,address,city,state,country);
		            
		        }
		    }
		
}
		


