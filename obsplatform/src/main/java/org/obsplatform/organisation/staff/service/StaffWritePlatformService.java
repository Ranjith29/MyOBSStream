/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.staff.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface StaffWritePlatformService {

    CommandProcessingResult createStaff(final JsonCommand command);

    CommandProcessingResult updateStaff(final Long staffId, final JsonCommand command);
}