package org.obsplatform.billing.chargevariant.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChargeVariantDetailsRepository extends

JpaRepository<ChargeVariantDetails, Long>,
JpaSpecificationExecutor<ChargeVariantDetails>{

}
