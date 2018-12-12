package org.obsplatform.portfolio.order.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderAddonsRepository  extends JpaRepository<OrderAddons, Long>,
   JpaSpecificationExecutor<OrderAddons>{
	
	public static final String ADDONCONSTANT = "from OrderAddons order where order.orderId =:orderId and order.isDelete='N'";

    @Query(ADDONCONSTANT)
    List<OrderAddons> findAddonsByOrderId(@Param("orderId") final Long orderId);
    
    @Query(ADDONCONSTANT +" and order.status='PENDING'")
    List<OrderAddons> findpendingAddonsByOrderId(@Param("orderId") final Long orderId);
    
    @Query(ADDONCONSTANT + " and order.status = 'ACTIVE'")
    List<OrderAddons> findTalkAddonsByOrderId(@Param("orderId") final Long orderId);
	
}
