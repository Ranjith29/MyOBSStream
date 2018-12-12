/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.commands.service;

import org.obsplatform.commands.domain.CommandSource;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface CommandProcessingService {

	CommandProcessingResult processAndLogCommand(CommandWrapper wrapper,
			JsonCommand command, boolean isApprovedByChecker);

	CommandProcessingResult logCommand(CommandSource commandSourceResult);

}
