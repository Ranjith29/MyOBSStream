/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.ApiParameterHelper;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.obsplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.obsplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.obsplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.obsplatform.organisation.staff.data.StaffData;
import org.obsplatform.organisation.staff.service.StaffReadPlatformService;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.client.service.GroupData;
import org.obsplatform.portfolio.client.service.LoanStatusMapper;
import org.obsplatform.portfolio.group.data.CenterData;
import org.obsplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.obsplatform.portfolio.group.data.GroupAccountSummaryData;
import org.obsplatform.portfolio.group.data.GroupGeneralData;
import org.obsplatform.portfolio.group.domain.GroupTypes;
import org.obsplatform.portfolio.group.exception.GroupNotFoundException;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class GroupReadPlatformServiceImpl implements GroupReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ReadReportingService readReportingService;

    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final ReadReportingService readReportingService) {
        
    	this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.centerReadPlatformService = centerReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.readReportingService=readReportingService;
    }

    @Override
    public GroupGeneralData retrieveTemplate(final Long officeId, final boolean isCenterGroup) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        Collection<CenterData> centerOptions = null;
        if (isCenterGroup) {
            centerOptions = this.centerReadPlatformService.retrieveAllForDropdown(defaultOfficeId);
        }

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        Collection<StaffData> staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        Collection<ClientData> clientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(defaultOfficeId);
        if (CollectionUtils.isEmpty(clientOptions)) {
            clientOptions = null;
        }

        final Long centerId = null;
        final String centerName = null;
        final Long staffId = null;
        final String staffName = null;

        return GroupGeneralData.template(defaultOfficeId, centerId, centerName, staffId, staffName, centerOptions, officeOptions,
                staffOptions, clientOptions);
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public Collection<GroupGeneralData> retrieveAll(final SearchParameters searchCriteria) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        String sql = "select " + this.allGroupTypesDataMapper.schema() + " where o.hierarchy like ?";

        final String extraCriteria = getGroupExtraCriteria(searchCriteria);
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        sql += " order by g.hierarchy";

        return this.jdbcTemplate.query(sql, this.allGroupTypesDataMapper, new Object[] { hierarchySearchString });
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(final SearchParameters searchCriteria) {

        String extraCriteria = " and g.level_Id = " + GroupTypes.GROUP.getId();

        String sqlSearch = searchCriteria.getSqlSearch();
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " g.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "g.display_name ");
            extraCriteria = " and (" + sqlSearch + ")";
        }

        final Long officeId = searchCriteria.getOfficeId();
        if (officeId != null) {
            extraCriteria += " and g.office_id = " + officeId;
        }

        final String externalId = searchCriteria.getExternalId();
        if (externalId != null) {
            extraCriteria += " and g.external_id = " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        final String name = searchCriteria.getName();
        if (name != null) {
            extraCriteria += " and g.display_name like " + ApiParameterHelper.sqlEncodeString("%" + name + "%");
        }

        final String hierarchy = searchCriteria.getHierarchy();
        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }

    @Override
    public GroupGeneralData retrieveOne(final Long groupId) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            String sql = "select " + this.allGroupTypesDataMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.allGroupTypesDataMapper, new Object[] { groupId, hierarchySearchString });
        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public GroupAccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId) {
        try {
            this.context.authenticatedUser();

            // Check if group exists
            // retrieveGroup(groupId);

            final List<GroupAccountSummaryData> pendingApprovalLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> awaitingDisbursalLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> openLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> closedLoans = new ArrayList<GroupAccountSummaryData>();

            final GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

            String sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is null";

            List<GroupAccountSummaryData> results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (final GroupAccountSummaryData row : results) {

                    final LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

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

            final List<GroupAccountSummaryData> pendingApprovalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> openIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> closedIndividualLoans = new ArrayList<GroupAccountSummaryData>();

            sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is not null";

            results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (final GroupAccountSummaryData row : results) {

                    final LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

                    if (statusMapper.isOpen()) {
                        openIndividualLoans.add(row);
                    } else if (statusMapper.isAwaitingDisbursal()) {
                        awaitingDisbursalIndividualLoans.add(row);
                    } else if (statusMapper.isPendingApproval()) {
                        pendingApprovalIndividualLoans.add(row);
                    } else {
                        closedIndividualLoans.add(row);
                    }
                }
            }

            return new GroupAccountSummaryCollectionData(pendingApprovalLoans, awaitingDisbursalLoans, openLoans, closedLoans,
                    pendingApprovalIndividualLoans, awaitingDisbursalIndividualLoans, openIndividualLoans, closedIndividualLoans);
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(final Long groupId, final Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if group exists
        // retrieveGroup(groupId);

        final GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

        final String sql = "select " + rm.loanAccountSummarySchema()
                + " where l.group_id = ? and l.client_id is null and l.loan_officer_id = ?";

        final List<GroupAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { groupId, loanOfficerId });

        return loanAccounts;
    }

    private static final class GroupLoanAccountSummaryDataMapper implements RowMapper<GroupAccountSummaryData> {

        public String loanAccountSummarySchema() {

            final StringBuilder accountsSummary = new StringBuilder("l.id as id, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId, ")
                    .append("l.account_no as accountNo ").append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public GroupAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
            final String accountNo = rs.getString("accountNo");
            return new GroupAccountSummaryData(id, externalId, productId, loanProductName, loanStatusId, accountNo);
        }
    }

	@Override
	public Collection<GroupData> retrieveGroupServiceDetails(Long orderId) {
		
		Map<String, String> queryParams=new HashMap<String, String>();
		   Collection<GroupData> codeDatas=new ArrayList<GroupData>();
		   queryParams.put("${orderId}", orderId.toString());
		final GenericResultsetData resultsetData=this.readReportingService.retrieveGenericResultset("GROUP_NAME", "parameter", queryParams, null);
		   List<ResultsetColumnHeaderData> columnHeaderDatas=resultsetData.getColumnHeaders();
		   List<ResultsetRowData> datas = resultsetData.getData();
		   
		   List<String> row;
		    Integer rSize;
		   for (int i = 0; i < datas.size(); i++) {
               row = datas.get(i).getRow();
               rSize = row.size();
               for (int j = 0; j < rSize-1; j++) {

            	   String  id=datas.get(i).getRow().get(j);
            	   j++;
            	   String paramValue=datas.get(i).getRow().get(j);
            	   j=j++;
            	   codeDatas.add(new GroupData(new Long(id), paramValue));
               }
           }
		   
		/*   for(int i=0;i<columnHeaderDatas.size();i++){
			   for(int j=0;j<datas.size();j++){
				   MCodeData codeData=new MCodeData();
				   if(columnHeaderDatas.get(i).getColumnName().equalsIgnoreCase("id")){
					   codeData.setmCodeValue(datas.get(i).getRow().get(i));
				   }else{
					   codeData.setmCodeValue(datas.get(i).getRow().get(i));
				   }
				   codeDatas.add(codeData);
			   }
		   }*/
		   
		   return codeDatas;
	}
}