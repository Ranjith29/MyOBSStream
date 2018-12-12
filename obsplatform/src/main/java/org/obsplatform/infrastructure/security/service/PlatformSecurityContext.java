/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.service;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.useradministration.domain.AppUser;

public interface PlatformSecurityContext {

    AppUser authenticatedUser();
    
    AppUser authenticatedUser(CommandWrapper commandWrapper);
    
    /**
     * Convenience method returns null (does not throw an exception) if an
     * authenticated user is not present
     * 
     * To be used only in service layer methods that can be triggered via both
     * the API and batch Jobs (which do not have an authenticated user)
     * 
     * @return
     */
    
    AppUser getAuthenticatedUserIfPresent();

    void validateAccessRights(String resourceOfficeHierarchy);

    String officeHierarchy();

   // boolean doesPasswordHasToBeRenewed(AppUser currentUser);
}