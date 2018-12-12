/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.useradministration.domain;

import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.infrastructure.security.domain.PlatformUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser>, PlatformUserRepository {

	
	@Query("Select appUser from AppUser appUser where appUser.username = :username and appUser.deleted = 0")
	AppUser findoneByAppUserName(@Param("username") String username);
	
/*    @Query("Select appUser from AppUser appUser where appUser.username = :username")
    AppUser findAppUserByName(@Param("username") String username);
	  */
}
