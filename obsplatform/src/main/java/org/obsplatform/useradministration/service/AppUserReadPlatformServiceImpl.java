/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.obsplatform.useradministration.data.AppUserData;
import org.obsplatform.useradministration.data.RoleData;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.obsplatform.useradministration.domain.Role;
import org.obsplatform.useradministration.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AppUserReadPlatformServiceImpl implements AppUserReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final RoleReadPlatformService roleReadPlatformService;
    private final AppUserRepository appUserRepository;

    @Autowired
    public AppUserReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, final RoleReadPlatformService roleReadPlatformService,
            final AppUserRepository appUserRepository) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.roleReadPlatformService = roleReadPlatformService;
        this.appUserRepository = appUserRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }

    @Override
    @Cacheable(value = "users", key = "T(org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy())")
    public Collection<AppUserData> retrieveAllUsers() {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserMapper mapper = new AppUserMapper();
        final String sql = "select " + mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString });
    }

    @Override
    public Collection<AppUserData> retrieveSearchTemplate() {
        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserLookupMapper mapper = new AppUserLookupMapper();
        final String sql = "select " + mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString });
    }

    @Override
    public AppUserData retrieveNewUserDetails() {

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAll();

        return AppUserData.template(offices, availableRoles);
    }

    @Override
    public AppUserData retrieveUser(final Long userId) {

        final AppUser user = this.appUserRepository.findOne(userId);
        if (user == null || user.isDeleted()) { throw new UserNotFoundException(userId); }

        Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAll();

        final Collection<RoleData> selectedUserRoles = new ArrayList<RoleData>();
        final Set<Role> userRoles = user.getRoles();
        for (final Role role : userRoles) {
            selectedUserRoles.add(role.toData());
        }

        availableRoles.removeAll(selectedUserRoles);

        return AppUserData.instance(user.getId(), user.getUsername(), user.getEmail(), user.getOffice().getId(),
                user.getOffice().getName(), user.getFirstname(), user.getLastname(), availableRoles, selectedUserRoles);
    }

    private static final class AppUserMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

            final Long id = resultSet.getLong("id");
            final String username = resultSet.getString("username");
            final String firstname = resultSet.getString("firstname");
            final String lastname = resultSet.getString("lastname");
            final String email = resultSet.getString("email");
            final Long officeId = JdbcSupport.getLong(resultSet, "officeId");
            final String officeName = resultSet.getString("officeName");

            return AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, null);
        }

        public String schema() {
            return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email,"
                    + " u.office_id as officeId, o.name as officeName from m_appuser u "
                    + " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
        }

    }

    private static final class AppUserLookupMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {

            final Long id = resultSet.getLong("id");
            final String username = resultSet.getString("username");

            return AppUserData.dropdown(id, username);
        }

        public String schema() {
            return " u.id as id, u.username as username from m_appuser u "
                    + " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
        }
    }
}