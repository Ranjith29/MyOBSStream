package org.obsplatform.finance.billingorder.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>,JpaSpecificationExecutor<Invoice> {


	@Query("from Invoice invoice where invoice.billId=:billNo and invoice.invoiceStatus='active' and invoice.dueAmount > 0")
	List<Invoice> findListofInvoicesIncludedInStatement(@Param("billNo") Long billNo);

}
