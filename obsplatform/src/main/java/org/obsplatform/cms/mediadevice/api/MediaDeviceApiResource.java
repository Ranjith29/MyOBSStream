package org.obsplatform.cms.mediadevice.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.cms.mediadetails.exception.NoMediaDeviceFoundException;
import org.obsplatform.cms.mediadevice.data.MediaDeviceData;
import org.obsplatform.cms.mediadevice.exception.NoPlanDataFoundException;
import org.obsplatform.cms.mediadevice.service.MediaDeviceReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/mediadevices")
@Component
@Scope("singleton")
public class MediaDeviceApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("deviceId", "clientId", "clientType", "balanceAmount", "balanceCheck"));
	private final Set<String> RESPONSE_DATA_PARAMETERS_FOR_PLAN = new HashSet<String>(Arrays.asList("id", "planCode", "planDescription"));
	private final String resourceNameForPermissions = "MEDIADEVICE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<MediaDeviceData> toApiJsonSerializer;
	private final DefaultToApiJsonSerializer<PlanData> toApiJsonSerializerForPlanData;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final MediaDeviceReadPlatformService mediaDeviceReadPlatformService;
	private final ConfigurationRepository configurationRepository;
	private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
    
	@Autowired
	public MediaDeviceApiResource(final PlatformSecurityContext context,
			final ConfigurationRepository configurationRepository,
			final DefaultToApiJsonSerializer<MediaDeviceData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final MediaDeviceReadPlatformService mediaDeviceReadPlatformService,
			final DefaultToApiJsonSerializer<PlanData> toApiJsonSerializerForPlanData,
			final ConfigurationRepository globalConfigurationRepository,
			final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.mediaDeviceReadPlatformService = mediaDeviceReadPlatformService;
		this.toApiJsonSerializerForPlanData = toApiJsonSerializerForPlanData;
		this.paymentGatewayConfigurationRepositoryWrapper = paymentGatewayConfigurationRepositoryWrapper;
		this.configurationRepository = configurationRepository;
	}

	@GET
	@Path("{deviceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleDeviceDetails(@PathParam("deviceId") final String deviceId,
			@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final MediaDeviceData datas = this.mediaDeviceReadPlatformService.retrieveDeviceDetails(deviceId);
		if (datas == null)
			throw new NoMediaDeviceFoundException();
		getTemplate(datas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());		
		return this.toApiJsonSerializer.serialize(settings, datas, RESPONSE_DATA_PARAMETERS);
	}
	
	private MediaDeviceData getTemplate(final MediaDeviceData mediaDeviceData) {
		final PaymentGatewayConfiguration paypalConfigData = this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK);
		final PaymentGatewayConfiguration paypalConfigDataForIos = this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK_IOS);
		final Configuration configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_BALANCE_CHECK);		
		mediaDeviceData.setPaypalConfigData(paypalConfigData);
		mediaDeviceData.setPaypalConfigDataForIos(paypalConfigDataForIos);
		mediaDeviceData.setBalanceCheck(configurationProperty.isEnabled());		
		return mediaDeviceData;
	}
		
	@GET
	@Path("{clientId}/prepaid")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePlanPrepaidDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<PlanData> datas = this.mediaDeviceReadPlatformService.retrievePlanDetails(clientId);
		if (datas == null) {
			throw new NoPlanDataFoundException();
		}
		PlanData planData = new PlanData(datas);
		if (!datas.isEmpty()) {
			planData.setIsActive(true);
			planData.setPlanCount(datas.size());
		} else {
			planData.setIsActive(false);
			planData.setPlanCount(datas.size());
		}
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerForPlanData.serialize(settings, planData, RESPONSE_DATA_PARAMETERS_FOR_PLAN);

	}
		
	@GET
	@Path("{clientId}/postpaid")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePostpaidPlanDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<PlanData> datas = this.mediaDeviceReadPlatformService.retrievePlanPostpaidDetails(clientId);
		if (datas == null) {
			throw new NoPlanDataFoundException();
		}
		PlanData planData = new PlanData(datas);

		if (!datas.isEmpty()) {
			planData.setIsActive(true);
			planData.setPlanCount(datas.size());
		} else {
			planData.setIsActive(false);
			planData.setPlanCount(datas.size());
		}
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerForPlanData.serialize(settings, planData, RESPONSE_DATA_PARAMETERS_FOR_PLAN);
	}
	
	@PUT
	@Path("{deviceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateStatus(@PathParam("deviceId") final String deviceId, final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateMediaStatus(deviceId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("client/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleDeviceDetailsBasedOnClientId(@PathParam("clientId") final String clientId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final MediaDeviceData datas = this.mediaDeviceReadPlatformService.retrieveClientDetails(clientId);
		if (datas == null)
			throw new NoMediaDeviceFoundException();

		getTemplate(datas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, datas, RESPONSE_DATA_PARAMETERS);
	}

	@PUT
	@Path("client/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateCrashDetails(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateMediaCrashDetails(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
}
