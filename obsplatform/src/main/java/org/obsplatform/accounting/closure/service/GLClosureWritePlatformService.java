/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.accounting.closure.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface GLClosureWritePlatformService {

    CommandProcessingResult createGLClosure(JsonCommand command);

    CommandProcessingResult updateGLClosure(Long glClosureId, JsonCommand command);

    CommandProcessingResult deleteGLClosure(Long glClosureId);

}
