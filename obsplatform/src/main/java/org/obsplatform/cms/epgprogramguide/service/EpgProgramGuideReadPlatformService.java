package org.obsplatform.cms.epgprogramguide.service;

import java.util.Date;
import java.util.List;

import org.obsplatform.cms.epgprogramguide.data.EpgProgramGuideData;

public interface EpgProgramGuideReadPlatformService {
	


	//public List<EpgProgramGuideData> retrivePrograms(String channelName, Long counter);

	List<EpgProgramGuideData> retrivePrograms(String channelName,Date progDate);
}
