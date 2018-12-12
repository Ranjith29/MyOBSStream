package org.obsplatform.billing.currency.domain;

import org.obsplatform.billing.currency.domain.CountryCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CountryCurrencyRepository extends
		JpaRepository<CountryCurrency, Long>,
		JpaSpecificationExecutor<CountryCurrency> {

}
