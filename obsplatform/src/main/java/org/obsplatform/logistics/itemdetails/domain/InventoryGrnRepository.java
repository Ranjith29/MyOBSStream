package org.obsplatform.logistics.itemdetails.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryGrnRepository extends JpaRepository<InventoryGrn, Long>, JpaSpecificationExecutor<InventoryGrn>{

}
