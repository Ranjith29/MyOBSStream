package org.obsplatform.portfolio.association.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.obsplatform.billing.emun.data.EnumValuesConstants;
import org.obsplatform.billing.emun.data.EnumValuesData;
import org.obsplatform.billing.emun.service.EnumReadplaformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/associations")
@Component
@Scope("singleton")
public class AssociationApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "planCode", "OrderId", "hardware", "serialNo",
					"planCode", "itemCode", "saleId", "itemId"));

	private final String resourceNameForPermissions = "ASSOCIATION";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<AssociationData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final EnumReadplaformService enumReadplaformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final MCodeReadPlatformService codeReadPlatformService;

	@Autowired
	public AssociationApiResource(
			final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<AssociationData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final HardwareAssociationReadplatformService associationReadplatformService,
			final EnumReadplaformService enumReadplaformService,
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final MCodeReadPlatformService codeReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.associationReadplatformService = associationReadplatformService;
		this.enumReadplaformService = enumReadplaformService;
		this.oneTimeSaleReadPlatformService=oneTimeSaleReadPlatformService;
		this.codeReadPlatformService=codeReadPlatformService;

	}

	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAssociationDetails(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<AssociationData> associationDatas = this.associationReadplatformService.retrieveClientAssociationDetails(clientId, null);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, associationDatas,RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("{clientId}/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAssociation(@PathParam("clientId") final Long clientId,@PathParam("id") final Long id, @Context final UriInfo uriInfo) {

		try {

			context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
			AssociationData associationData = this.associationReadplatformService.retrieveSingleDetails(id);
			Collection<EnumValuesData> enumValuesDatas = this.enumReadplaformService.getEnumValues(EnumValuesConstants.ENUMVALUE_PROPERTY_DEVICE_SWAP);
			List<AssociationData> HardwareDatas = this.associationReadplatformService.retrieveHardwareData(clientId);
			List<AssociationData> planDatas = this.associationReadplatformService.retrieveplanData(clientId);
			HardwareDatas.add(new AssociationData(associationData.getSerialNum(), associationData.getProvisionNumber(),
					associationData.getAllocationType(), associationData.getPropertyCode(), associationData.getOrderId()));

			AssociationData data = new AssociationData(associationData.getPlanId(), associationData.getPlanCode(),associationData.getOrderId());
			planDatas.add(data);
			associationData.addHardwareDatas(HardwareDatas);
			associationData.addPlanDatas(planDatas);
			associationData.addEnumValuesDatas(enumValuesDatas, null,null);
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return this.toApiJsonSerializer.serialize(settings,associationData, RESPONSE_DATA_PARAMETERS);
		} catch (NullPointerException n) {
			return null;
		}
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAssociationTemplate(@QueryParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
 		 
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<AssociationData> HardwareDatas = this.associationReadplatformService.retrieveCustomerHardwareAllocationData(clientId,null,null);
		List<AssociationData> planDatas= this.associationReadplatformService.retrieveplanData(clientId);
		AssociationData data=new AssociationData(HardwareDatas,planDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);

	}

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addAssociation(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createAssociation(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("{associationId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateAssociation(@PathParam("associationId") final Long associationId,final String apiRequestBodyAsJson) {
		
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAssociation(associationId).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);
	}	

	@PUT
	@Path("deassociation/{associationId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deAssoicationHardware(@PathParam("associationId") final Long associationId,final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDeAssociation(associationId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("{clientId}/serialNum/{serialNumber}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAssociationWithSerialNum(@PathParam("clientId") final Long clientId,@PathParam("serialNumber") final String serialNumber,@Context final UriInfo uriInfo) {

		try {

			context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
			AssociationData associationData = this.associationReadplatformService.retrieveAssociationsDetailsWithSerialNum(clientId,serialNumber);
			Collection<EnumValuesData> enumValuesDatas = this.enumReadplaformService.getEnumValues(EnumValuesConstants.ENUMVALUE_PROPERTY_DEVICE_SWAP);
			
			Collection<MCodeData> itemCategory = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_ITEM_CATEGORY);
			final List<ItemData> itemCategoryTypeData = this.oneTimeSaleReadPlatformService.retrieveItemByCategoryType(associationData.getItemId());
			
			associationData.addEnumValuesDatas(enumValuesDatas ,itemCategoryTypeData, itemCategory);
			LocalDate toDayDate = new LocalDate();
			LocalDate warrantyDate = associationData.getWarrantyDate();
			if(!warrantyDate.isBefore(toDayDate)){
				final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
				return this.toApiJsonSerializer.serialize(settings,associationData, RESPONSE_DATA_PARAMETERS);
			}else{
				throw new PlatformDataIntegrityException( "device.warranty.expired.", 
						"Device Warranty Expired");
			}
			
		} catch (NullPointerException n) {
			return null;
		}
	}
	
}