package org.obsplatform.cms.journalvoucher.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JournalvoucherRepository extends JpaRepository<JournalVoucher,Long>
  ,JpaSpecificationExecutor<JournalVoucher>{

}
