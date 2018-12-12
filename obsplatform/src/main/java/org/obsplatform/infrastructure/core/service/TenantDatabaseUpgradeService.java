/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.security.service.TenantDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.googlecode.flyway.core.Flyway;



/**
 * A service that picks up on tenants that are configured to auto-update their
 * specific schema on application startup.
 */
@Service
public class TenantDatabaseUpgradeService {

    private final TenantDetailsService tenantDetailsService;
    protected final DataSource tenantDataSource;

    @Autowired
    public TenantDatabaseUpgradeService(final TenantDetailsService detailsService, 
    		@Qualifier("tenantDataSourceJndi") final DataSource dataSource) {
        this.tenantDetailsService = detailsService;
        this.tenantDataSource = dataSource;
    }

    @PostConstruct
    public void upgradeAllTenants() {
        List<ObsPlatformTenant> tenants = tenantDetailsService.findAllTenants();
        for (ObsPlatformTenant tenant : tenants) {
            if (tenant.isAutoUpdateEnabled()) {
                Flyway flyway = new Flyway();
                flyway.setDataSource(tenant.databaseURL(), tenant.getSchemaUsername(), tenant.getSchemaPassword());
                flyway.setLocations("sql");
                flyway.repair();
                flyway.setOutOfOrder(true);
                flyway.migrate();
              
            }
        }
    }
}