package org.obsplatform.organisation.voucher.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 
 * @author ashokreddy
 *
 */

public interface VoucherRepository extends JpaRepository<Voucher, Long>,
JpaSpecificationExecutor<Voucher>{
	

}
