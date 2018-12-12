package org.obsplatform.portfolio.hardwareswapping.service;

import java.util.List;

import org.obsplatform.portfolio.association.data.AssociationData;

public interface HardwareSwappingReadplatformService {

	Boolean retrieveingDisconnectionOrders(final String serialNumber);
	
	Boolean retrieveingPendingOrders(final String serialNumber);
	
	List<AssociationData> retrievingAllAssociations(final Long clientId,final String serialNumber,Long orderId);

}

