package org.obsplatform.portfolio.order.service;

import java.util.List;

import org.obsplatform.portfolio.order.data.OrderAddonsData;

public interface OrderAddOnsReadPlaformService {

	List<OrderAddonsData> retrieveAllOrderAddons(Long orderId);

}
