/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.isexdirectory.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.isexdirectory.service.IsExDirectoryWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Naresh
 *
 */
@Service
public class CreateIsExDirectoryCommandHandler implements NewCommandSourceHandler {

    private final IsExDirectoryWritePlatformService isExDirectoryWritePlatformService;

    @Autowired
    public CreateIsExDirectoryCommandHandler(final IsExDirectoryWritePlatformService isExDirectoryWritePlatformService) {
        this.isExDirectoryWritePlatformService = isExDirectoryWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.isExDirectoryWritePlatformService.createIsExDirectory(command);
    }
}