/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.group.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface GroupingTypesWritePlatformService {

    CommandProcessingResult createCenter(JsonCommand command);

    CommandProcessingResult updateCenter(Long entityId, JsonCommand command);

    CommandProcessingResult createGroup(Long centerId, JsonCommand command);

    CommandProcessingResult activateGroupOrCenter(Long entityId, JsonCommand command);

    CommandProcessingResult updateGroup(Long groupId, JsonCommand command);

    CommandProcessingResult deleteGroup(Long groupId);

    CommandProcessingResult unassignStaff(Long grouptId, JsonCommand command);
}
