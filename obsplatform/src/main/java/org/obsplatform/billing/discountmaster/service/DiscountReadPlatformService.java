package org.obsplatform.billing.discountmaster.service;

import java.util.List;

import org.obsplatform.billing.discountmaster.data.DiscountDetailData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;

/**
 * @author hugo
 * 
 */
public interface DiscountReadPlatformService {

	List<DiscountMasterData> retrieveAllDiscounts();

	DiscountMasterData retrieveSingleDiscountDetail(Long discountId);

	List<DiscountDetailData> retrieveDiscountdetails(Long discountId);



}
