package org.obsplatform.organisation.partner.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfo;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfoRepository;
import org.obsplatform.organisation.partner.exception.AgreementNotFoundException;
import org.obsplatform.organisation.partner.service.PartnersWritePlatformServiceImp;
import org.obsplatform.organisation.partner.domain.Agreement;
import org.obsplatform.organisation.partner.domain.AgreementDetails;
import org.obsplatform.organisation.partner.domain.AgreementDetailsRepository;
import org.obsplatform.organisation.partner.domain.AgreementRepository;
import org.obsplatform.organisation.partner.serialization.PartnersAgreementCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class PartnersAgreementWritePlatformServiceImp implements PartnersAgreementWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(PartnersWritePlatformServiceImp.class);
	
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;
	private final PartnersAgreementCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final AgreementRepository agreementRepository;
	private final OfficeAdditionalInfoRepository officeAdditionalInfoRepository;
	private final AgreementDetailsRepository agreementDetailsRepository;
	
	
	@Autowired
	public PartnersAgreementWritePlatformServiceImp(final PlatformSecurityContext context,final FromJsonHelper fromApiJsonHelper,
			final PartnersAgreementCommandFromApiJsonDeserializer apiJsonDeserializer,final AgreementRepository agreementRepository,
		    final OfficeAdditionalInfoRepository officeAdditionalInfoRepository,final AgreementDetailsRepository agreementDetailsRepository) {
		
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.agreementRepository = agreementRepository;
		this.officeAdditionalInfoRepository = officeAdditionalInfoRepository;
		this.agreementDetailsRepository = agreementDetailsRepository;

	}

	@Override
	public CommandProcessingResult createNewPartnerAgreement(final JsonCommand command) {

		
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final OfficeAdditionalInfo additionalInfo=this.officeAdditionalInfoRepository.findOne(command.entityId());
			Agreement agreement=Agreement.fromJosn(command,additionalInfo.getOffice().getId());
			final JsonArray partnerAgreementArray = command.arrayOfParameterNamed("sourceData").getAsJsonArray();
			
			if(partnerAgreementArray.size() !=0){
			for(int i=0; i<partnerAgreementArray.size();i++){ 
				
				final JsonElement element = fromApiJsonHelper.parse(partnerAgreementArray.get(i).toString());
				final Long source = fromApiJsonHelper.extractLongNamed("source", element);
				final Long serviceId = fromApiJsonHelper.extractLongNamed("serviceId", element);
				final String shareType = fromApiJsonHelper.extractStringNamed("shareType", element);
				final BigDecimal shareAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("shareAmount",element);
				final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
				final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
				AgreementDetails details=new AgreementDetails(source,shareType,shareAmount,startDate,endDate, serviceId);
				agreement.addAgreementDetails(details);
			}
		 }
			this.agreementRepository.save(agreement);
		
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
			                          .withEntityId(agreement.getId()).withOfficeId(agreement.getOfficeId()).build();
		  }catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);
		
		if(dve.getMostSpecificCause().getMessage().contains("agreement_dtl_ai_ps_mc_uniquekey")){
			 throw new PlatformDataIntegrityException("error.msg.agreement.duplicate.source.data.entry.issue","A Agreement with sourceCategory " +
			 		"already exists","source");
		} else if(dve.getMostSpecificCause().getMessage().contains("serviceid_agreementid_uq")){
			 throw new PlatformDataIntegrityException("error.msg.agreement.duplicate.service.data.entry.issue","A Agreement with ServiceCode " +
				 		"already exists","serviceId");
		}else {
			throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "+ realCause.getMessage());
		}

	}
//
	@Override
	public CommandProcessingResult UpdatePartnerAgreement(final JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForUpdate(command.json());
			Agreement agreement=this.agreementRetrieveById(command.entityId());
			final Map<String, Object> changes = agreement.update(command);
			final JsonArray partnerAgreementArray = command.arrayOfParameterNamed("sourceData").getAsJsonArray();
			final JsonArray removeAgreementDetails = command.arrayOfParameterNamed("removeSourceData").getAsJsonArray();
			
			if (removeAgreementDetails.size() != 0) {
				for (int i = 0; i < removeAgreementDetails.size(); i++) {
					final JsonElement element = fromApiJsonHelper.parse(removeAgreementDetails.get(i).toString());
					final Long detailId = fromApiJsonHelper.extractLongNamed("detailId", element);
					AgreementDetails detail = this.agreementDetailsRepository.findOne(detailId);
					/*detail.setSourceType(Long.valueOf((detail.getSourceType().toString()+detail.getId().toString())));
					detail.setEndDate(DateUtils.getDateOfTenant());*/
					detail.setServiceId(null);
					detail.setSourceType(null);
					detail.setIsDeleted('Y');
					this.agreementDetailsRepository.saveAndFlush(detail);
				}
			}

			this.agreementRepository.saveAndFlush(agreement);
			
			  if(partnerAgreementArray.size() !=0){
				 for(int i=0; i<partnerAgreementArray.size(); i++){
					 
						final JsonElement element = fromApiJsonHelper.parse(partnerAgreementArray.get(i).toString());
						final Long detailId = fromApiJsonHelper.extractLongNamed("detailId", element);
						final Long source = fromApiJsonHelper.extractLongNamed("source", element);
						final Long serviceId = fromApiJsonHelper.extractLongNamed("serviceId", element);
						final String shareType = fromApiJsonHelper.extractStringNamed("shareType", element);
						final BigDecimal shareAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("shareAmount",element);
						final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
						final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
						if(detailId !=null){
						AgreementDetails detail=this.agreementDetailsRepository.findOne(detailId);
						detail.setSourceType(source);
						detail.setShareType(shareType);
						detail.setShareAmount(shareAmount);
						detail.setStartDate(startDate.toDate());
						detail.setServiceId(serviceId);
						if(endDate!=null){detail.setEndDate(endDate.toDate());}
						this.agreementDetailsRepository.saveAndFlush(detail);
						}else{
							AgreementDetails details=new AgreementDetails(source,shareType,shareAmount,startDate,endDate, serviceId);
							agreement.addAgreementDetails(details);
						}
				}
			}
		  
			  this.agreementRepository.saveAndFlush(agreement);
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(agreement.getId())
					 .withOfficeId(agreement.getOfficeId()).with(changes).build();
		  
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}	   
        
   }

	@Override
	public CommandProcessingResult deletePartnerAgreement(final Long agreementId) {

		try {
			context.authenticatedUser();
			final Agreement agreement = this.agreementRetrieveById(agreementId);
			List<AgreementDetails> details = agreement.getDetails();
			for (AgreementDetails detail : details) {
				detail.setSourceType(Long.valueOf((detail.getSourceType().toString() + detail.getId().toString())));
				detail.setEndDate(DateUtils.getDateOfTenant());
				detail.setIsDeleted('Y');
			}
			agreement.delete();
			this.agreementRepository.save(agreement);
			return new CommandProcessingResultBuilder()
					.withEntityId(agreement.getId())
					.withOfficeId(agreement.getOfficeId()).build();

		} catch (DataIntegrityViolationException dve) {

			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}
	
	
	private Agreement agreementRetrieveById(final Long entityId) {

		Agreement agreement = this.agreementRepository.findOne(entityId);
		if (agreement == null) {
			throw new AgreementNotFoundException(entityId);
		}
		return agreement;
	}
	
}
