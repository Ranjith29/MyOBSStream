package org.obsplatform.portfolio.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;;

public interface OrderPriceRepository extends

JpaRepository<OrderPrice, Long>,
JpaSpecificationExecutor<OrderPrice>{
	
	public static final String ADDONCONSTANT = "from OrderPrice orderPrice where orderPrice.id =:orderPriceId and orderPrice.isDeleted='N'";

    @Query(ADDONCONSTANT)
    OrderPrice findOneByOldOrder(@Param("orderPriceId") final Long orderPriceId);

}
