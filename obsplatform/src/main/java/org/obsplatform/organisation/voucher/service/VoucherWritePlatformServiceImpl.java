package org.obsplatform.organisation.voucher.service;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang.RandomStringUtils;
import org.obsplatform.cms.eventorder.exception.InsufficientAmountException;
import org.obsplatform.cms.eventorder.service.EventOrderWriteplatformService;
import org.obsplatform.cms.journalvoucher.domain.JournalVoucher;
import org.obsplatform.cms.journalvoucher.domain.JournalvoucherRepository;
import org.obsplatform.finance.billingorder.service.BillingOrderWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeRepository;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.obsplatform.organisation.voucher.domain.Voucher;
import org.obsplatform.organisation.voucher.domain.VoucherDetails;
import org.obsplatform.organisation.voucher.domain.VoucherDetailsRepository;
import org.obsplatform.organisation.voucher.domain.VoucherRepository;
import org.obsplatform.organisation.voucher.exception.AlreadyProcessedException;
import org.obsplatform.organisation.voucher.exception.UnableToCancelVoucherException;
import org.obsplatform.organisation.voucher.exception.VoucherDetailsNotFoundException;
import org.obsplatform.organisation.voucher.exception.VoucherLengthMatchException;
import org.obsplatform.organisation.voucher.serialization.VoucherCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 * @author rakesh
 *
 */
@Service
public class VoucherWritePlatformServiceImpl implements VoucherWritePlatformService {
	
	private int remainingKeyLength;
	private char status = 0;
	private Voucher voucher;
	private String generatedKey;
	private Long quantity;
	private String type;
	private char enable = 'N';
	

	private static final String ALPHA = "Alpha";
	private static final String NUMERIC = "Numeric";
	private static final String TYPE_PRODUCT = "PRODUCT";
	private static final String  TYPE_VALUE = "VALUE";
	private static final String ALPHANUMERIC = "AlphaNumeric";
	
	private final PlatformSecurityContext context;
	private final VoucherRepository voucherRepository;
	private final VoucherDetailsRepository voucherDetailsRepository;
	private final EventOrderWriteplatformService eventOrderWriteplatformService;
	private final VoucherCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final VoucherReadPlatformService voucherReadPlatformService;
	private final OfficeRepository officeRepository;
	private final JournalvoucherRepository journalvoucherRepository;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService; 
	
	@Autowired
	public VoucherWritePlatformServiceImpl(final PlatformSecurityContext context,final VoucherRepository voucherRepository,
			final VoucherReadPlatformService voucherReadPlatformService,final VoucherCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final VoucherDetailsRepository voucherDetailsRepository,final OfficeRepository officeRepository,final JournalvoucherRepository journalvoucherRepository,
			final EventOrderWriteplatformService eventOrderWriteplatformService,final BillingOrderWritePlatformService billingOrderWritePlatformService) {
		
		this.context = context;
		this.voucherRepository = voucherRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.voucherReadPlatformService = voucherReadPlatformService;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.eventOrderWriteplatformService = eventOrderWriteplatformService;
		this.voucherDetailsRepository=voucherDetailsRepository;
		this.journalvoucherRepository = journalvoucherRepository;
		this.officeRepository = officeRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult createRandomGenerator(final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			
			final Long officeId = command.longValueOfParameterNamed("officeId");
            final Office clientOffice = this.officeRepository.findOne(officeId);

            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }
			
			final Long length = command.bigDecimalValueOfParameterNamed("length").longValue();
			final String beginWith = command.stringValueOfParameterNamed("beginWith");
			final int bwLength = beginWith.trim().length();
			
			if(bwLength == length.intValue()){
				
				throw new VoucherLengthMatchException();
			}
				
			Voucher voucherpin = Voucher.fromJson(command);
			voucherpin.setOfficeId(officeId);
			
			this.voucherRepository.save(voucherpin);	
			return new CommandProcessingResult(voucherpin.getId());

		}  catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}  catch (ParseException e) {
			return CommandProcessingResult.empty();
		}
		
	}
	
	@Transactional
	@Override
	public CommandProcessingResult generateVoucherPinKeys(final Long batchId) {
		
		try{
			voucher = this.voucherRepository.findOne(batchId);
			if(voucher == null){
				throw new PlatformDataIntegrityException("error.msg.code.batchId.not.found", "VoucherBatch with id :'" + batchId + "'does not exists", "batchId", batchId);
			} 
			if(voucher.getIsProcessed() == enable){
				status = 'F';
				final Long voucherId = generateRandomNumbers();
				status = 'Y';
				return new CommandProcessingResult(voucherId);
			} else{	
				throw new AlreadyProcessedException("VoucherPin Already Generated with this " + voucher.getBatchName());
			}
		} finally{
			if(voucher != null && voucher.getIsProcessed() == 'N'){
				voucher.setIsProcessed(status);
				this.voucherRepository.save(voucher);
			}
		}
		
	}

	public Long generateRandomNumbers() {
		
		final Long lengthofVoucher = voucher.getLength();
		
		final int length = (int)lengthofVoucher.longValue();
		
		
		quantity = voucher.getQuantity();
		
		type = voucher.getPinCategory();
		
		final int beginKeyLength = voucher.getBeginWith().length();
		
		remainingKeyLength = length - beginKeyLength;
		
		if(remainingKeyLength == 0){
			
			throw new VoucherLengthMatchException();
		}
		
		final Long serialNo = voucher.getSerialNo();
		
		String minSerialSeries = "";
		String maxSerialSeries = "";
		
		for (int serialNoValidator = 0; serialNoValidator < serialNo; serialNoValidator++) {
			
			if (serialNoValidator > 0) {
				minSerialSeries = minSerialSeries + "0";
				maxSerialSeries = maxSerialSeries + "9";
			} else {
				minSerialSeries = minSerialSeries + "1";
				maxSerialSeries = maxSerialSeries + "9";
			}
		}

		final Long minNo = Long.parseLong(minSerialSeries);
		final Long maxNo = Long.parseLong(maxSerialSeries);

		long currentSerialNumber = this.voucherReadPlatformService.retrieveMaxNo(minNo, maxNo);

		if (currentSerialNumber == 0) {
			currentSerialNumber = minNo;
		}

		return randomValueGeneration(currentSerialNumber);

	}
	
	private Long randomValueGeneration(Long currentSerialNumber) {
		
		int quantityValidator;

		for (quantityValidator = 0; quantityValidator < quantity; quantityValidator++) {
			
			String name = voucher.getBeginWith() + generateRandomSingleCode();

			String value = this.voucherReadPlatformService.retrieveIndividualPin(name);
			
			if (value == null) {
				
				currentSerialNumber = currentSerialNumber + 1;
				
				VoucherDetails voucherDetails = new VoucherDetails(name, currentSerialNumber, voucher);
				
				this.voucherDetailsRepository.save(voucherDetails);

			} else {
				quantityValidator = quantityValidator - 1;
			}

		}

		return voucher.getId();
	}
	
	private String generateRandomSingleCode() {
		
		if (type.equalsIgnoreCase(ALPHA)) {			
			generatedKey = RandomStringUtils.randomAlphabetic(remainingKeyLength);			
		} 
		
		if (type.equalsIgnoreCase(NUMERIC)) {
			generatedKey = RandomStringUtils.randomNumeric(remainingKeyLength);
		}
		
		if (type.equalsIgnoreCase(ALPHANUMERIC)) {
			generatedKey = RandomStringUtils.randomAlphanumeric(remainingKeyLength);
		}
		
		return generatedKey;
		
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("batch_name")) {
			final String name = command
					.stringValueOfParameterNamed("batchName");
			throw new PlatformDataIntegrityException("error.msg.code.duplicate.batchname", "A batch with name'"
							+ name + "'already exists", "displayName", name);
		}
		if (realCause.getMessage().contains("serial_no_key")) {
			throw new PlatformDataIntegrityException(
					"error.msg.code.duplicate.serial_no_key", "A serial_no_key already exists", "displayName", "serial_no");
		}

		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause.getMessage());
	}
	
	
	@Override
	public CommandProcessingResult updateUpdateVoucherPins(Long voucherId, JsonCommand command) {
		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForUpdate(command.json(), true);
			
			final String[] services = command.arrayValueOfParameterNamed("voucherIds");
			final String status = command.stringValueOfParameterNamed("status");
			
			for (final String id : services) {
				
				final VoucherDetails voucherpinDetails = voucherDetailsRetrieveById(Long.valueOf(id));
				if(!voucherpinDetails.getStatus().equalsIgnoreCase("USED")){
					voucherpinDetails.setStatus(status);
					this.voucherDetailsRepository.save(voucherpinDetails);
				}
			}
			
			return new CommandProcessingResult(voucherId);

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}

	}

	private VoucherDetails voucherDetailsRetrieveById(final Long id) {

		final VoucherDetails voucherDetails = this.voucherDetailsRepository.findOne(id);

		if (voucherDetails == null) {
			throw new VoucherDetailsNotFoundException(id);
		}
		return voucherDetails;
	}

	@Override
	public CommandProcessingResult deleteUpdateVoucherPins(Long voucherId, JsonCommand command) {

		try {
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForUpdate(command.json(), false);
			
			final String[] services = command.arrayValueOfParameterNamed("voucherIds");
			
			for (final String id : services) {
				final VoucherDetails voucherpinDetails = voucherDetailsRetrieveById(Long.valueOf(id));
				if(!voucherpinDetails.getStatus().equalsIgnoreCase("USED")){
					voucherpinDetails.setIsDeleted('Y');
					this.voucherDetailsRepository.save(voucherpinDetails);
				}
			}
			
			return new CommandProcessingResult(voucherId);

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}
	}

	@Override
	public CommandProcessingResult cancelVoucherPins(Long entityId,JsonCommand command) {
              
		try{
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCancel(command.json(), false);
			VoucherDetails voucherDetails=this.voucherDetailsRetrieveById(entityId);
			voucher = voucherDetails.getVoucher();
			BigDecimal value=new BigDecimal(voucher.getPinValue());
			
			if(voucher.getPinType().equalsIgnoreCase(TYPE_VALUE)){
				boolean isSufficient = this.eventOrderWriteplatformService.checkClientBalance(value.doubleValue(),voucherDetails.getClientId(), true);
				if(!isSufficient){
					throw new InsufficientAmountException("cancelvoucher");
				}
				this.billingOrderWritePlatformService.updateClientBalance(value,voucherDetails.getClientId(), true);
			}else if(voucher.getPinType().equalsIgnoreCase(TYPE_PRODUCT)){
				throw new UnableToCancelVoucherException();
			}
			
			
			 JournalVoucher journalVoucher=new JournalVoucher(voucherDetails.getId(),DateUtils.getDateOfTenant(),"VOUCHER CANCEL",null,value.doubleValue(),
					 voucherDetails.getClientId());
				this.journalvoucherRepository.save(journalVoucher);
				
				voucherDetails.update(command.stringValueOfParameterNamed("cancelReason"));
				this.voucherDetailsRepository.save(voucherDetails);
				
				return new CommandProcessingResult(voucherDetails.getId());
				
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
			
	}

}
