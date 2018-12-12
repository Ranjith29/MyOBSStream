/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.monetary.data.CurrencyData;
import org.obsplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.data.OfficeTransactionData;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service("employeeSearchService")
public class OfficeReadPlatformServiceImpl implements OfficeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final static String NAMEDECORATEDBASEON_HIERARCHY = "concat(substring('........................................', 1, ((LENGTH(o.hierarchy) - LENGTH(REPLACE(o.hierarchy, '.', '')) - 1) * 4)), o.name)";

    @Autowired
    public OfficeReadPlatformServiceImpl(final PlatformSecurityContext context,
    		final CurrencyReadPlatformService currencyReadPlatformService, final RoutingDataSource dataSource) {
        this.context = context;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class OfficeMapper implements RowMapper<OfficeData> {

        public String officeSchema() {
            return "o.id AS id,o.name AS name,"
            	       +NAMEDECORATEDBASEON_HIERARCHY+
            	       "AS nameDecorated,o.external_id AS externalId,o.opening_date AS openingDate,o.hierarchy AS hierarchy," +
            	       "parent.id AS parentId,parent.name AS parentName,c.code_value as officeType, ifnull(b.balance_amount,0 )as balance, " +
            	       "od.state as state,od.city as city,od.country as country,od.phone_number as phoneNumber,od.office_number as officeNumber,od.email_id as email " +
            	       " FROM m_office o LEFT JOIN m_office AS parent ON parent.id = o.parent_id  join m_code_value c on c.id=o.office_type and c.code_value='Office' " +
            	       " Left join m_office_balance b on o.id = b.office_id  Left join b_office_address od ON o.id = od.office_id  ";
        }

        @Override
        public OfficeData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

            final Long id = resultSet.getLong("id");
            final String name = resultSet.getString("name");
            final String nameDecorated = resultSet.getString("nameDecorated");
            final String externalId = resultSet.getString("externalId");
            final LocalDate openingDate = JdbcSupport.getLocalDate(resultSet, "openingDate");
            final String hierarchy = resultSet.getString("hierarchy");
            final Long parentId = JdbcSupport.getLong(resultSet, "parentId");
            final String parentName = resultSet.getString("parentName");
            final String officeType = resultSet.getString("officeType");
            final BigDecimal balance =resultSet.getBigDecimal("balance");
            final String city =resultSet.getString("city");
        	final String state =resultSet.getString("state");
        	final String country =resultSet.getString("country");
        	final String email =resultSet.getString("email");
        	final String phoneNumber =resultSet.getString("phoneNumber");
        	final String officeNumber =resultSet.getString("officeNumber");

            return new OfficeData(id, name, nameDecorated, externalId, openingDate, hierarchy, parentId, parentName, null, null, officeType,balance,null,city,state,country,email,phoneNumber,officeNumber);
        }
    }

    private static final class OfficeDropdownMapper implements RowMapper<OfficeData> {

        public String schema() {
            return " o.id as id, " + NAMEDECORATEDBASEON_HIERARCHY + " as nameDecorated, o.name as name from m_office o ";
        }

        @Override
        public OfficeData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

            final Long id = resultSet.getLong("id");
            final String name = resultSet.getString("name");
            final String nameDecorated = resultSet.getString("nameDecorated");

            return OfficeData.dropdown(id, name, nameDecorated);
        }
    }

    private static final class OfficeTransactionMapper implements RowMapper<OfficeTransactionData> {

        public String schema() {
            return " ot.id as id, ot.transaction_date as transactionDate, ot.from_office_id as fromOfficeId, fromoff.name as fromOfficeName, "
                    + " ot.to_office_id as toOfficeId, tooff.name as toOfficeName, ot.transaction_amount as transactionAmount, ot.description as description, "
                    + " ot.currency_code as currencyCode, rc.decimal_places as currencyDigits, "
                    + " rc.name as currencyName, rc.internationalized_name_code as currencyNameCode, rc.display_symbol as currencyDisplaySymbol "
                    + " from m_office_transaction ot "
                    + " left join m_office fromoff on fromoff.id = ot.from_office_id "
                    + " left join m_office tooff on tooff.id = ot.to_office_id " + " join m_currency rc on rc.`code` = ot.currency_code";
        }

        @Override
        public OfficeTransactionData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

            final Long id = resultSet.getLong("id");
            final LocalDate transactionDate = JdbcSupport.getLocalDate(resultSet, "transactionDate");
            final Long fromOfficeId = JdbcSupport.getLong(resultSet, "fromOfficeId");
            final String fromOfficeName = resultSet.getString("fromOfficeName");
            final Long toOfficeId = JdbcSupport.getLong(resultSet, "toOfficeId");
            final String toOfficeName = resultSet.getString("toOfficeName");
            final String currencyCode = resultSet.getString("currencyCode");
            final String currencyName = resultSet.getString("currencyName");
            final String currencyNameCode = resultSet.getString("currencyNameCode");
            final String currencyDisplaySymbol = resultSet.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(resultSet, "currencyDigits");

            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol, currencyNameCode);

            final BigDecimal transactionAmount = resultSet.getBigDecimal("transactionAmount");
            final String description = resultSet.getString("description");

            return OfficeTransactionData.instance(id, transactionDate, fromOfficeId, fromOfficeName, toOfficeId, toOfficeName,
                    currencyData, transactionAmount, description);
        }
    }

    @Override
    public Collection<OfficeData> retrieveAllOffices() {

        final AppUser currentUser = context.authenticatedUser();

        String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = hierarchy + "%";

        final OfficeMapper officeMapper = new OfficeMapper();
        final String sql = "select " + officeMapper.officeSchema() + " where o.hierarchy like ? order by o.hierarchy";

        return this.jdbcTemplate.query(sql, officeMapper , new Object[] { hierarchySearchString });
    }

    @Override
    public Collection<OfficeData> retrieveAllOfficesForDropdown() {
        final AppUser currentUser = context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final OfficeDropdownMapper officeDropdownMap = new OfficeDropdownMapper();
        final String sql = "select " + officeDropdownMap.schema() + "where o.hierarchy like ? order by o.name";

        return this.jdbcTemplate.query(sql, officeDropdownMap , new Object[] { hierarchySearchString });
    }

    @Override
    public OfficeData retrieveOffice(final Long officeId) {

        try {
            context.authenticatedUser();

            final OfficeMapper officeMapper = new OfficeMapper();
            final String sql = "select " + officeMapper.officeSchema() + " where o.id = ?";

            return this.jdbcTemplate.queryForObject(sql, officeMapper , new Object[] { officeId });
        } catch (EmptyResultDataAccessException e) {
            throw new OfficeNotFoundException(officeId);
        }
    }

    @Override
    public OfficeData retrieveNewOfficeTemplate() {

        context.authenticatedUser();

        return OfficeData.template(null, DateUtils.getLocalDateOfTenant(), null);
    }

    @Override
    public Collection<OfficeData> retrieveAllowedParents(final Long officeId) {

        context.authenticatedUser();
        final Collection<OfficeData> filterParentLookups = new ArrayList<OfficeData>();

        if (isNotHeadOffice(officeId)) {
            final Collection<OfficeData> parentLookups = retrieveAllOfficesForDropdown();

            for (final OfficeData office : parentLookups) {
                if (!office.hasIdentifyOf(officeId)) {
                    filterParentLookups.add(office);
                }
            }
        }

        return filterParentLookups;
    }

    private boolean isNotHeadOffice(final Long officeId) {
        return !Long.valueOf(1).equals(officeId);
    }

    @Override
    public Collection<OfficeTransactionData> retrieveAllOfficeTransactions() {

        final AppUser currentUser = context.authenticatedUser();

        String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = hierarchy + "%";

        OfficeTransactionMapper officeTransactionMap = new OfficeTransactionMapper();
        String sql = "select " + officeTransactionMap.schema()
                + " where (fromoff.hierarchy like ? or tooff.hierarchy like ?) order by ot.transaction_date, ot.id";

        return this.jdbcTemplate.query(sql, officeTransactionMap , new Object[] { hierarchySearchString, hierarchySearchString });
    }

    @Override
    public OfficeTransactionData retrieveNewOfficeTransactionDetails() {
        context.authenticatedUser();

        final Collection<OfficeData> parentLookups = retrieveAllOfficesForDropdown();
        final Collection<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();

        return OfficeTransactionData.template(DateUtils.getLocalDateOfTenant(), parentLookups, currencyOptions);
    }

	@Override
	public List<OfficeData> retrieveAgentTypeData() {

        final AppUser currentUser = context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final OfficeDropdownMapper officeDropdownMap = new OfficeDropdownMapper();
        final String sql = "select " + officeDropdownMap.schema() + ", m_code_value c  WHERE o.office_type = c.id AND c.code_value = 'agent' AND o.hierarchy LIKE ? " +
        		" ORDER BY o.name";

        return this.jdbcTemplate.query(sql, officeDropdownMap , new Object[] { hierarchySearchString });
    
	}
	
	@Override
	public Collection<FinancialTransactionsData> retreiveOfficeFinancialTransactionsData(final Long officeId) {
		
		context.authenticatedUser();
		final OfficeFinancialTransactionMapper mapper = new OfficeFinancialTransactionMapper(); 
		final String sql = "select v.* from  office_fin_trans_vw v where v.office_id=" + officeId + " order by  transDate desc ";
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}
	
	private static final class OfficeFinancialTransactionMapper implements RowMapper<FinancialTransactionsData> {

		@Override
		public FinancialTransactionsData mapRow(final ResultSet resultSet, final int rowNum)throws SQLException {
			final Long officeId = resultSet.getLong("office_id");
			final Long transactionId = resultSet.getLong("TransId");
			final String transactionType = resultSet.getString("TransType");
			final BigDecimal debitAmount = resultSet.getBigDecimal("Dr_amt");
			final BigDecimal creditAmount =resultSet.getBigDecimal("Cr_amt");
			final String userName = resultSet.getString("username");
			final String transactionCategory = resultSet.getString("tran_type");
			final boolean flag = resultSet.getBoolean("flag");
			final LocalDate transDate = JdbcSupport.getLocalDate(resultSet, "TransDate");

			return new FinancialTransactionsData(officeId, transactionId, transDate, transactionType, debitAmount, creditAmount, 
					null, userName, transactionCategory, flag, null, null,false);
		}
     }

	@Override
	public Collection<OfficeData> retrieveOfficeTypeData() {
		final AppUser currentUser = context.authenticatedUser();

		final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final OfficeDropdownMapper officeDropdownMap = new OfficeDropdownMapper();
        final String sql = "select " + officeDropdownMap.schema() + ", m_code_value c  WHERE o.office_type = c.id AND c.code_value = 'office' AND o.hierarchy LIKE ? " +
        		" ORDER BY o.name";

        return this.jdbcTemplate.query(sql, officeDropdownMap , new Object[] { hierarchySearchString });
	}
	
	
	@Override
	public Collection<OfficeData> retrieveAllParentOffices() {
		
		context.authenticatedUser();
		final ParentOfficesMapper mapper = new ParentOfficesMapper();
		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class ParentOfficesMapper implements RowMapper<OfficeData> {

		public String schema() {
			return " mo.id as id, mo.name as name from m_office mo where mo.partner_type_id is null ";
		}

		@Override
		public OfficeData mapRow(ResultSet resultSet, int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String name = resultSet.getString("name");
			final OfficeData officeData = new OfficeData(id, name, null, null, null, null, null, null, null, 
					null, null, null, null, null, null, null, null, null, null);

			return officeData;
		}
	}
}