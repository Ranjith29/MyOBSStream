package org.obsplatform.cms.media.api;

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

import org.obsplatform.cms.media.data.MediaAssetData;
import org.obsplatform.cms.media.data.MediaassetAttribute;
import org.obsplatform.cms.media.data.MediaassetAttributeData;
import org.obsplatform.cms.media.service.MediaAssetReadPlatformService;
import org.obsplatform.cms.mediadetails.data.MediaLocationData;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.data.MediaEnumoptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/assets")
@Component
@Scope("singleton")
public class MediaAssestApiResource {
	
	private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("mediaId","mediaTitle","type","classType","overview","subject",
    		"mediaImage","duration","contentProvider","rated","mediaRating","ratingCount","location","status","releaseDate","genres","languages","filmLocations",
    		"writers","directors","actors","countries","noOfPages","mediaDetails","mediaStatus","mediaAttributes","mediaFormat","mediaTypeData","mediaCategeorydata",
    		"mediaLanguageData"));
	
    private final String resourceNameForPermissions = "MEDIAASSET";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<MediaAssetData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final MediaAssetReadPlatformService mediaAssetReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	
	private final PlanReadPlatformService planReadPlatformService;
	 @Autowired
	    public MediaAssestApiResource(final PlatformSecurityContext context, final DefaultToApiJsonSerializer<MediaAssetData> toApiJsonSerializer,
	    final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
	    final MediaAssetReadPlatformService mediaAssetReadPlatformService,final PlanReadPlatformService planReadPlatformService,
	    final MCodeReadPlatformService mCodeReadPlatformService) {
		        this.context = context;
		        this.toApiJsonSerializer = toApiJsonSerializer;
		        this.apiRequestParameterHelper = apiRequestParameterHelper;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        this.mediaAssetReadPlatformService=mediaAssetReadPlatformService;
		        this.planReadPlatformService=planReadPlatformService;
		        this.mCodeReadPlatformService=mCodeReadPlatformService;
		    }	
	 
	 
	//Get All Media Asset Details
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveMediaAssestdata(@QueryParam("deviceId") final String deviceId, @QueryParam("pageNo")  Long pageNum,
			@QueryParam("filterType") final String filterType, @QueryParam("clientType") final String clientType, @Context final UriInfo uriInfo) {
		
          context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        //  MediaDeviceData details=this.deviceReadPlatformService.retrieveDeviceDetails(deviceId);
          Long pageNo = Long.valueOf(0);
          Long noOfPages = Long.valueOf(0);
          if(pageNum == null || pageNum == 0){
        	  pageNum = Long.valueOf(0);
          }else{
        	  pageNo = (pageNum * 10);
          }
          List<MediaAssetData> data = new ArrayList<MediaAssetData>();
          if("ALL".equalsIgnoreCase(filterType)){
        	   
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdata(pageNo,clientType);
        	   
        	  final String queryFOrPages = " SELECT count(0)  FROM b_media_asset m inner join b_mod_detail ed on ed.media_id = m.id"
			                   +" inner join b_mod_master em on em.id = ed.event_id  GROUP BY m.id  having  count( ed.media_id) = 1 ";
         	  noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(queryFOrPages);
         	  for(final MediaAssetData assetData:data){
         		  
         		  //List<MediaLocationData> locationData=this.mediaAssetReadPlatformService.retrievemediaAssetLocationdata(assetData.getMediaId());
         	  }
         	  
         	  //data.add(new MediaAssetData(noOfPages,pageNum));
        	  
          }
          else if("RELEASE".equalsIgnoreCase(filterType)){
        	  
		     data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyNewRealease(pageNo);
		     final String query = " SELECT count(0) FROM b_media_asset m INNER JOIN b_mod_detail ed ON ed.media_id = m.id"
		    		 +" INNER JOIN b_mod_master em  ON em.id = ed.event_id where m.release_date <= adddate('"+DateUtils.getDateTimeOfTenant()+"',INTERVAL -3 MONTH)"
		    		 +" group by m.id  having count(distinct ed.event_id) >=1 ";
	          noOfPages=this.mediaAssetReadPlatformService.retrieveNoofPages(query);
	        // data.add(new MediaAssetData(noOfPages,pageNum));
		     
          }
          else if("RATING".equalsIgnoreCase(filterType)){
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyRating(pageNo);
        	  final String query = " SELECT count(0) FROM b_media_asset m INNER JOIN b_mod_detail ed ON ed.media_id = m.id"
        			  +" INNER JOIN b_mod_master em ON em.id = ed.event_id group by m.id  having count(distinct ed.event_id) >=1 ";
	           noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
	          //data.add(new MediaAssetData(noOfPages,pageNum));
          }
          else if("DISCOUNT".equalsIgnoreCase(filterType)){
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyDiscountedMovies(pageNo);
        	  final String query = " SELECT count(0) FROM b_media_asset m INNER JOIN b_mod_detail ed ON ed.media_id = m.id"
        			  +" INNER JOIN b_mod_master em  ON em.id = ed.event_id inner join  b_mod_pricing ep on em.id=ep.event_id"
        			  +" where discount_id>=1  group by m.id  having count(distinct ed.event_id) >=1";
	           noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
	          //data.add(new MediaAssetData(noOfPages,pageNum));
          }
          else if("PROMOTION".equalsIgnoreCase(filterType)){
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyPromotionalMovies(pageNo);
        	  final String query = " SELECT count(0)  FROM b_media_asset m inner join b_mod_detail ed on ed.media_id = m.id"
	                   +" inner join b_mod_master em on em.id = ed.event_id  group by m.id  having count(distinct ed.event_id) >1 ";
	           noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
	         // data.add(new MediaAssetData(noOfPages,pageNum));
          } 
          else if("COMING".equalsIgnoreCase(filterType)){
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyComingSoonMovies(pageNo);
        	  final String query = " SELECT count(0) FROM b_media_asset m where category_id=19 ";
	           noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
	         // data.add(new MediaAssetData(noOfPages,pageNum));
          }
          else if("WATCHED".equalsIgnoreCase(filterType)){
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabyMostWatchedMovies(pageNo);
        	  final String query = "SELECT count(0) FROM b_media_asset m inner join b_mod_detail ed on m.id=ed.media_id  inner " +
        	  		" JOIN b_modorder eo  ON (eo.event_id = ed.event_id)";
        	   noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
        	//  data.add(new MediaAssetData(noOfPages,pageNum));
          }         
          else {
        	  
        	  data = this.mediaAssetReadPlatformService.retrievemediaAssetdatabySearching(pageNo,filterType);
        	  final String query = "SELECT count(0) FROM b_media_asset m inner join b_mod_detail ed on m.id=ed.media_id  inner " +
        	  		" JOIN b_modorder eo  ON (eo.event_id = ed.event_id)";
        	   noOfPages = this.mediaAssetReadPlatformService.retrieveNoofPages(query);
        	  //data.add(new MediaAssetData(noOfPages,pageNum));
          }
          final MediaAssetData mediaAssetData = new MediaAssetData(data,noOfPages, pageNum);
          final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
          return this.toApiJsonSerializer.serialize(settings, mediaAssetData, RESPONSE_DATA_PARAMETERS);
	}
	   
    @GET
    @Path("mediadata")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllMediaAssestdata(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final List<MediaAssetData> data = this.mediaAssetReadPlatformService.retrieveAllAssetdata();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveMediaAssestTemplatedata(@Context final UriInfo uriInfo) {
    	context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final MediaAssetData assetData = handleMediaAssestTemplateData();
        assetData.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, assetData, RESPONSE_DATA_PARAMETERS);
    }
    
    private MediaAssetData handleMediaAssestTemplateData() {
    	 final List<EnumOptionData> status = this.planReadPlatformService.retrieveNewStatus();
    	 final List<MediaassetAttribute> data   = this.mediaAssetReadPlatformService.retrieveMediaAttributes();
    	 final List<MediaassetAttribute> mediaFormat = this.mediaAssetReadPlatformService.retrieveMediaFormatType();
    	 final List<MediaEnumoptionData> mediaTypeData = this.mediaAssetReadPlatformService.retrieveMediaTypeData();
    	 final Collection<MCodeData> eventCategeorydata = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_EVENT_CATEGORY);
    	 final List<McodeData> mediaCategeorydata=this.mediaAssetReadPlatformService.retrieveMedaiCategory();
    	 final List<McodeData> languageCategeory=this.mediaAssetReadPlatformService.retrieveLanguageCategeories();
    	 final List<McodeData> contentProviderData=this.mediaAssetReadPlatformService.retrieveContentProviders();
         return new MediaAssetData(null, null, null, status, data, mediaFormat, eventCategeorydata, mediaCategeorydata, languageCategeory, 
        		 contentProviderData, mediaTypeData);
	}
    
	@GET
    @Path("{mediaId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleMediaAssestDetails(@PathParam("mediaId") final Long mediaId, @Context final UriInfo uriInfo) {
         context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
       
         final MediaAssetData mediaAssetData = this.mediaAssetReadPlatformService.retrievemediaAsset(mediaId);
         final List<MediaassetAttributeData> mediaassetAttributes = this.mediaAssetReadPlatformService.retrieveMediaassetAttributesData(mediaId);
         final List<MediaLocationData> mediaLocationData = this.mediaAssetReadPlatformService.retrievemediaAssetLocationdata(mediaId);
         final List<EnumOptionData> status = this.planReadPlatformService.retrieveNewStatus();
         final List<MediaassetAttribute> data = this.mediaAssetReadPlatformService.retrieveMediaAttributes();
         final List<MediaassetAttribute> mediaFormat = this.mediaAssetReadPlatformService.retrieveMediaFormatType();
         final List<MediaEnumoptionData> mediaTypeData = this.mediaAssetReadPlatformService.retrieveMediaTypeData();
         final Collection<MCodeData> eventCategeorydata = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_EVENT_CATEGORY);
         final List<McodeData> mediaCategeorydata = this.mediaAssetReadPlatformService.retrieveMedaiCategory();
         final List<McodeData> mediaLanguageData = this.mediaAssetReadPlatformService.retrieveLanguageCategeories();
         final List<McodeData> contentProviderData = this.mediaAssetReadPlatformService.retrieveContentProviders();
         final MediaAssetData assetData = new MediaAssetData(mediaAssetData,mediaassetAttributes,mediaLocationData,status,data,mediaFormat,eventCategeorydata,mediaCategeorydata,mediaLanguageData,contentProviderData,mediaTypeData);
         final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
         return this.toApiJsonSerializer.serialize(settings, assetData, RESPONSE_DATA_PARAMETERS);
    }
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createMediaAssetData(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createMediaAsset().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@PUT
	@Path("{assetId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	
	public String updateMediaAssetData(@PathParam("assetId") final Long assetId, final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateMediaAsset(assetId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("{assetId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteMediaAssetData(@PathParam("assetId") final Long assetId, final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteMediaAsset(assetId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * This method used for creating media attributes and locations
	 * Now we are not using
	 * whenever you requires use it
	 * */
	@POST
	@Path("locationAttributes/{assetId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createMediaLocationsAndAttributes(@PathParam("assetId") final Long assetId, final String apiRequestBodyAsJson) {

	   final CommandWrapper commandRequest = new CommandWrapperBuilder().createMediaAssetLocationAttribute(assetId).withJson(apiRequestBodyAsJson).build();
	   final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	   return this.toApiJsonSerializer.serialize(result);
	}
	 	
}
