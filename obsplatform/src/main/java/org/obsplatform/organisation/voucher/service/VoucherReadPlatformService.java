package org.obsplatform.organisation.voucher.service;

import java.util.List;

import javax.ws.rs.core.StreamingOutput;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.voucher.data.VoucherData;

/**
 * 
 * @author ashokreddy
 *
 */
public interface VoucherReadPlatformService {

	String retrieveIndividualPin(String pinId);
	
	Page<VoucherData> getAllVoucherById(SearchSqlQuery searchTicketMaster, String statusType, Long id);

	List<EnumOptionData> pinCategory();

	List<EnumOptionData> pinType();

	Long retrieveMaxNo(Long minNo, Long maxNo);

	StreamingOutput retrieveVocherDetailsCsv(Long batchId);

	List<VoucherData> retrivePinDetails(String pinNumber);

	List<VoucherData> getAllData();


}
