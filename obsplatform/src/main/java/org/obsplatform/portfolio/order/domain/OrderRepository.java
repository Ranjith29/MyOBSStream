package org.obsplatform.portfolio.order.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository  extends JpaRepository<Order, Long>,
   JpaSpecificationExecutor<Order>{

    @Query("from Order order where order.id=(select max(newOrder.id) from Order newOrder where newOrder.orderNo =:orderNo and newOrder.status=3 )")
	Order findOldOrderByOrderNO(@Param("orderNo")String orderNo);

    @Query("from Order order where order.id=(select max(newOrder.id) from Order newOrder where newOrder.orderNo =:orderNo)")
	Order findOneOrderByOrderNO(@Param("orderNo")String orderNo);

    @Query("from Order order where order.id=(SELECT min(bo.id) AS minOrderId FROM Order bo WHERE bo.clientId =:clientId" +
    		"  AND bo.planId =:planId AND bo.status in (1,4) AND bo.isDeleted='n')")
	Order findOnePrimaryActiveOrderDetails(@Param("clientId")Long clientId, @Param("planId")Long planId);
    
    @Query("from Order order where order.clientId = :clientId and order.isDeleted = 'N' ")
    List<Order> findOneByClientId(@Param("clientId")Long clientId);
}
