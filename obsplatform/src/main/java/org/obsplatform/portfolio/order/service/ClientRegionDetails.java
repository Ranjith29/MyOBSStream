package org.obsplatform.portfolio.order.service;

import org.obsplatform.organisation.priceregion.data.PriceRegionData;
import org.obsplatform.organisation.priceregion.service.RegionalPriceReadplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ClientRegionDetails {
	
	private final RegionalPriceReadplatformService regionalPriceReadplatformService;
	public static final String CLIENT_REGION="ALL";
	
	@Autowired
	public ClientRegionDetails(final RegionalPriceReadplatformService regionalPriceReadplatformService) {
		
		this.regionalPriceReadplatformService=regionalPriceReadplatformService;
	}

	
	public String getTheClientRegionDetails(Long clientId) {
		
		try{
			PriceRegionData priceRegionData=this.regionalPriceReadplatformService.getTheClientRegionDetails(clientId);
			if(priceRegionData!=null)
				return priceRegionData.getPriceregion();
			else
				 return CLIENT_REGION;
				
		}catch(Exception e){
			return null;
		}
		
		
	}

}
