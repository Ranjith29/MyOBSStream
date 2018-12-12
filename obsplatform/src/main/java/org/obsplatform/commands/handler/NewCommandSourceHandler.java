/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.commands.handler;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Interface for processing {@link JsonCommand}
 * 
 * @author pavani
 *
 */
public interface NewCommandSourceHandler {

    /**
     * Method for Processing {@link JsonCommand}
     * 
     * @param command
     * @return
     */
    CommandProcessingResult processCommand(JsonCommand command);
}
