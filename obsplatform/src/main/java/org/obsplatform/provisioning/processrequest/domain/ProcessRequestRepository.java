package org.obsplatform.provisioning.processrequest.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessRequestRepository extends JpaRepository<ProcessRequest, Long>, JpaSpecificationExecutor<ProcessRequest> {

 @Query( "FROM ProcessRequest PR WHERE PR.isProcessed='N' AND PR.isNotify='N' AND PR.requestType='CHANGE CREDENTIALS' AND PR.clientId = :clientId")
 ProcessRequest findOutExistsChangeCredentialsRequest(@Param("clientId") Long clientId);
 
 @Query( "FROM ProcessRequest PR WHERE PR.isProcessed='N' AND PR.isNotify='N' AND PR.requestType='CHANGE EXDIRECTORY' AND PR.orderId = :orderId")
 ProcessRequest findOutExistsChangeExDirectoryRequest(@Param("orderId") Long orderId);

 @Query( "FROM ProcessRequest PR WHERE PR.isProcessed='N' AND PR.isNotify='N' AND (PR.requestType='ADDON_ACTIVATION' OR PR.requestType='ADDON_DISCONNECTION')  AND PR.orderId = :orderId")
 List<ProcessRequest> findOutExistsChangeOrderAddOnsRequest(@Param("orderId") Long orderId);
}