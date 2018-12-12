package org.obsplatform.scheduledjobs.dataupload.service;

import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.scheduledjobs.dataupload.command.DataUploadCommand;


public interface DataUploadWritePlatformService {


	CommandProcessingResult addItem(DataUploadCommand command);
	
	
	CommandProcessingResult processDatauploadFile(Long fileId);

	
	
	

}
