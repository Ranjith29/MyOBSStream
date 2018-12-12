package org.obsplatform.portfolio.client.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientCardDetailsRepository extends JpaRepository<ClientCardDetails, Long>, JpaSpecificationExecutor<ClientCardDetails> {

	@Query("from ClientCardDetails cd where cd.client=:client and cd.type='PseudoCard' and cd.rtftype='I'")
	ClientCardDetails findOneByClient(@Param("client")Client client);
	
	@Query("from ClientCardDetails cd where cd.client=:client and cd.type='ObfuscatedCard' and cd.isDeleted='N'")
	ClientCardDetails findOneByClientObfuscatedCard(@Param("client")Client client);
}
