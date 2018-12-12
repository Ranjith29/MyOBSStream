package org.obsplatform.portfolio.plan.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.obsplatform.billing.emun.data.EnumValuesConstants;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.cms.eventmaster.service.EventMasterReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.codes.service.CodeReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.partner.data.PartnersData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.plan.data.BillRuleData;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.data.PlanQulifierData;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.obsplatform.portfolio.service.service.ServiceMasterReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
* 
*
* @author istream
*/
@Path("/plans")
@Component
@Scope("singleton")
public class PlansApiResource  {
	
//Response Data parameters
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "planCode", "plan_description", "startDate","isHwReq",
            "endDate", "status", "service_code", "service_description","Period", "charge_code", "charge_description","servicedata","contractPeriod","provisionSystem",
            "service_type", "charge_type", "allowedtypes","selectedservice","bill_rule","billiingcycle","servicedata","services","statusname","planstatus","volumeTypes"));
	 
		private static final String RESOURCE_NAME_FOR_PERMISSION="PLAN";//resourceNameForPermissions = "PLAN";
		private  final  PlatformSecurityContext context;
	    private final DefaultToApiJsonSerializer<PlanData> toApiJsonSerializer;
	    private final DefaultToApiJsonSerializer<PlanQulifierData> apiJsonSerializer;
	    private final ApiRequestParameterHelper apiRequestParameterHelper;
	    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	    private final PlanReadPlatformService planReadPlatformService;
	    private final ServiceMasterReadPlatformService serviceMasterReadPlatformService;
	    private final MCodeReadPlatformService mCodeReadPlatformService;
	    private final CodeReadPlatformService codeReadPlatformService;
	    private final EventMasterReadPlatformService eventMasterReadPlatformService;
	    private final ClientReadPlatformService clientReadPlatformService;
	    
	   
	    @Autowired
	    public PlansApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<PlanData> toApiJsonSerializer,
	    		final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
	    		final PlanReadPlatformService planReadPlatformService,final ServiceMasterReadPlatformService serviceMasterReadPlatformService,
	    		final MCodeReadPlatformService mCodeReadPlatformService,final CodeReadPlatformService codeReadPlatformService,
	    		final DefaultToApiJsonSerializer<PlanQulifierData> apiJsonSerializer, final EventMasterReadPlatformService eventMasterReadPlatformService,
	    		final ClientReadPlatformService clientReadPlatformService) {
	    	
		        this.context = context;
		        this.toApiJsonSerializer = toApiJsonSerializer;
		        this.mCodeReadPlatformService=mCodeReadPlatformService;
		        this.planReadPlatformService=planReadPlatformService;
		        this.apiRequestParameterHelper = apiRequestParameterHelper;
		        this.serviceMasterReadPlatformService=serviceMasterReadPlatformService;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        this.codeReadPlatformService = codeReadPlatformService;
		        this.apiJsonSerializer = apiJsonSerializer;
		        this.eventMasterReadPlatformService = eventMasterReadPlatformService;
		        this.clientReadPlatformService = clientReadPlatformService;
		    }	
	    
	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createPlan(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest=new CommandWrapperBuilder().createPlan().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result=this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		  return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePlanTemplate(@Context final UriInfo uriInfo) {
		 
		context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
		PlanData planData=null;
		planData=handleTemplateData(planData);
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, planData, RESPONSE_DATA_PARAMETERS);
	
	}
	
	
	private PlanData handleTemplateData(PlanData planData) {
		
		 final List<ServiceData> data = this.serviceMasterReadPlatformService.retrieveAllServices("N");
		 final List<EventMasterData> events = this.eventMasterReadPlatformService.retrieveEventMasterData();
	     final List<BillRuleData> billData = this.codeReadPlatformService.retrievebillRules(EnumValuesConstants.ENUMVALUE_PROPERTY_BILLING_RULES);

		 final List<EnumOptionData> status = this.planReadPlatformService.retrieveNewStatus();
		 final Collection<MCodeData> provisionSysData = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROVISIONING);
		 final List<EnumOptionData> volumeType = this.planReadPlatformService.retrieveVolumeTypes();
		 final List<PlanData> clientcategorysDatas = planReadPlatformService.retrieveClientCategories();
		 List<ServiceData> services = new ArrayList<>();
		 List<EventMasterData> selectedEvents = new ArrayList<>();
		 List<PlanData> clientCategorys = new ArrayList<>();
		 
		 if(planData != null){
			 
				 services = this.planReadPlatformService.retrieveSelectedServices(planData.getId());
				 int size = data.size();
					final int selectedsize = services.size();
						for (int i = 0; i < selectedsize; i++)
			     			{
							final Long selected = services.get(i).getId();
							for (int j = 0; j < size; j++) {
								final Long avialble = data.get(j).getId();
								if (selected.equals(avialble)) {
									data.remove(j);
									size--;
								}
							}
						}
				clientCategorys = this.planReadPlatformService .retrieveSelectedClientCategorys(planData.getId());
						 int sizeClientCategory = clientcategorysDatas.size();
							final int selectedsizeClientCategory = clientCategorys.size();
								for (int i = 0; i < selectedsizeClientCategory; i++)
					     			{
									final Long selectedData = clientCategorys.get(i).getId();
									for (int j = 0; j < sizeClientCategory; j++) {
										final Long avialbleClientCategorys = clientcategorysDatas.get(j).getId();
										if (selectedData.equals(avialbleClientCategorys)) {
											clientcategorysDatas.remove(j);
											sizeClientCategory--;
										}
									}
								}
				 selectedEvents = this.planReadPlatformService.retrieveSelectedEvents(planData.getId());
				 int sizeEvent = events.size();
					final int selectedsizeEvent = selectedEvents.size();
						for (int i = 0; i < selectedsizeEvent; i++)
			     			{
							final Long selected = selectedEvents.get(i).getId();
							for (int j = 0; j < sizeEvent; j++) {
								final Long avialble = events.get(j).getId();
								if (selected.equals(avialble)) {
									events.remove(j);
									sizeEvent--;
								}
							}
						}
				
		 }		 
		 return new PlanData(data, billData, null, status, planData, services,provisionSysData,volumeType, DateUtils.getLocalDateOfTenantForClient(), events, selectedEvents, clientcategorysDatas, clientCategorys);	
	}

	
	/**
	 * @param planType
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllPlans(@QueryParam("planType") final String planType,  @Context final UriInfo uriInfo) {
 		 context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
		final List<PlanData> products = this.planReadPlatformService.retrievePlanData(planType);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, products, RESPONSE_DATA_PARAMETERS);
	}
	
	/**
	 * @param planId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("{planId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePlanDetails(@PathParam("planId") final Long planId,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
		PlanData singlePlandata = this.planReadPlatformService.retrievePlanData(planId);
		singlePlandata=handleTemplateData(singlePlandata);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, singlePlandata, RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param planId
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@PUT
	@Path("{planCode}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updatePlan(@PathParam("planCode") final Long planId,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePlan(planId).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		  return this.toApiJsonSerializer.serialize(result);
	}
	
	
	 /**
	 * @param planId
	 * @return
	 */
	   @DELETE
		@Path("{planCode}")
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String deletePlan(@PathParam("planCode") final Long planId) {
		  final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePlan(planId).build();
          final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
          return this.toApiJsonSerializer.serialize(result);

		}
	   
	    @GET
		@Path("planQulifier/{planId}")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String retrievePlanQulifierDetails(@PathParam("planId") final Long planId,@Context final UriInfo uriInfo) {
			
			context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
			
			List<PartnersData> partnersDatas =this.planReadPlatformService.retrievePartnersData(planId);
			List<PartnersData> availablePartnersDatas = this.planReadPlatformService.retrieveAvailablePartnersData(planId);
			PlanQulifierData data=new PlanQulifierData(partnersDatas,availablePartnersDatas);  
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return this.apiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
		}
	    
	    @PUT
		@Path("planQulifier/{planId}")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String updatePlanQulifierData(@PathParam("planId") final Long planId,final String apiRequestBodyAsJson) {
			 final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePlanQualifier(planId).withJson(apiRequestBodyAsJson).build();
			 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			  return this.toApiJsonSerializer.serialize(result);
		}
}