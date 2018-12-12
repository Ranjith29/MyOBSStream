package org.obsplatform.scheduledjobs.dataupload.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class UploadStatusExist extends AbstractPlatformDomainRuleException{

	public UploadStatusExist(final String serialNumber){
		
		super("Item is already existing with item SerialNumber", 
				"Item is already existing with item SerialNumber:"+serialNumber,serialNumber);
	}
}