package org.obsplatform.cms.mediadetails.service;

import java.util.List;

import org.obsplatform.cms.eventprice.data.EventPriceData;
import org.obsplatform.cms.mediadetails.data.MediaAssetDetailsData;
import org.obsplatform.cms.mediadetails.data.MediaAssetLocationDetails;
import org.obsplatform.cms.mediadetails.data.MediaLocationData;

public interface MediaAssetDetailsReadPlatformService {


	List<String> retrieveGenresData(Long mediaId);

	List<String> retrieveProductions(Long mediaId);

	List<MediaLocationData> retrieveFilmLocation(Long mediaId);

	List<String> retrieveWriters(Long mediaId);

	List<String> retrieveDirectors(Long mediaId);

	List<String> retrieveActors(Long mediaId);

	List<String> retrieveCountry(Long mediaId);
	
	MediaAssetDetailsData retrieveMediaAssetDetailsData(Long category);
	
	List<MediaAssetLocationDetails> retrieveMediaAssetLocationData(Long mediaId);

	List<EventPriceData> getEventPriceDetails(Long eventId, String clientType);

}
