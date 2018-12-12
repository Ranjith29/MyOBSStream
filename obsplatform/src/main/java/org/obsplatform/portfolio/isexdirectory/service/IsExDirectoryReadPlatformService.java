package org.obsplatform.portfolio.isexdirectory.service;

import org.obsplatform.portfolio.isexdirectory.data.IsExDirectoryData;

/**
 * 
 * @author Naresh
 * 
 */
public interface IsExDirectoryReadPlatformService {

	IsExDirectoryData retrieveIsExDirectoryByOrderId(Long orderId);

}
