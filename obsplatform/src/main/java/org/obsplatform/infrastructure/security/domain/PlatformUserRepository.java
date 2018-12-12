/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.domain;

import org.obsplatform.infrastructure.security.domain.PlatformUser;

public interface PlatformUserRepository {

    PlatformUser findByUsernameAndDeletedAndEnabled(String username, boolean deleted, boolean enabled);

}