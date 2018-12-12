package org.obsplatform.organisation.hardwareplanmapping.service;

import java.util.List;

import org.obsplatform.organisation.hardwareplanmapping.data.HardwareMappingDetailsData;

public interface HardwareMappingReadPlatformService {

	List<HardwareMappingDetailsData> getPlanDetailsByItemCode(String itemCode,
			Long clientId);

}
