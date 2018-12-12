package org.obsplatform.organisation.address.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CityRepository extends JpaRepository<City,Long>,JpaSpecificationExecutor<City>{

}
