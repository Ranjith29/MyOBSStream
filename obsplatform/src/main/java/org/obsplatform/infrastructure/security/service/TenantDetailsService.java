/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.service;

import java.util.List;

import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;

public interface TenantDetailsService {

    ObsPlatformTenant loadTenantById(String tenantId);

    List<ObsPlatformTenant> findAllTenants();

	void updateLicenseKey(String licenseKey);

}
