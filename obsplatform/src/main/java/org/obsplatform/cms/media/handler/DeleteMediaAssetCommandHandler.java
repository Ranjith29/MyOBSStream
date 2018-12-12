package org.obsplatform.cms.media.handler;

import org.obsplatform.cms.media.service.MediaAssetWritePlatformService;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteMediaAssetCommandHandler  implements NewCommandSourceHandler{

	private final MediaAssetWritePlatformService writePlatformService;

    @Autowired
    public DeleteMediaAssetCommandHandler(final MediaAssetWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }
	
    @Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		
		 return this.writePlatformService.deleteMediaAsset(command);
	}

}
