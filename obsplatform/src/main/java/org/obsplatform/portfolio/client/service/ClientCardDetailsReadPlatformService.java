package org.obsplatform.portfolio.client.service;

import java.util.List;

import org.obsplatform.portfolio.client.data.ClientCardDetailsData;

public interface ClientCardDetailsReadPlatformService {

	List<ClientCardDetailsData> retrieveClientDetails(final Long clientId);

	ClientCardDetailsData retrieveClient(final Long id, final String type, final Long clientId);
}
