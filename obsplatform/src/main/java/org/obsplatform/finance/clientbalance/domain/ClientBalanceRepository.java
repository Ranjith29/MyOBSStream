package org.obsplatform.finance.clientbalance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientBalanceRepository extends JpaRepository<ClientBalance, Long>,
                         JpaSpecificationExecutor<ClientBalance>{
	
	@Query("from ClientBalance clientBalance where clientBalance.clientId =:clientId") 
	ClientBalance findByClientId(@Param("clientId") final Long clientId);


}
