/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.codes.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeValueRepository extends JpaRepository<CodeValue, Long>, JpaSpecificationExecutor<CodeValue> {

   
	CodeValue findByCodeNameAndId(String codeName, Long id);
    
	@Query("from CodeValue codeValue where codeValue.label =:codeName")
	CodeValue findOneByCodeValue(@Param("codeName") String codeName);

}
