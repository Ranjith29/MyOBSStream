/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.domain;

import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<AppUser> {

    @Autowired
    private AppUserRepository userRepository;

    @Override
    public AppUser getCurrentAuditor() {

        AppUser currentUser = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                currentUser = (AppUser) authentication.getPrincipal();
            } else {
                currentUser = this.retrieveSuperUser();
            }
        } else {
            currentUser = this.retrieveSuperUser();
        }
        return currentUser;
    }

    private AppUser retrieveSuperUser() {
        return this.userRepository.findOne(Long.valueOf("1"));
    }
}
