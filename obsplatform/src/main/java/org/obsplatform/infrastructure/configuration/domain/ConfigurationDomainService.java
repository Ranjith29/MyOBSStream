/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.configuration.domain;

import org.obsplatform.infrastructure.cache.domain.CacheType;

public interface ConfigurationDomainService {

    boolean isMakerCheckerEnabledForTask(String taskPermissionCode);
    boolean isEhcacheEnabled();
    void updateCache(CacheType cacheType);
	boolean isConstraintApproachEnabledForDatatables();
}