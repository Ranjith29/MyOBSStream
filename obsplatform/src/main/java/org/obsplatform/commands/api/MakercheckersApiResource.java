/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.commands.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.commands.data.AuditData;
import org.obsplatform.commands.data.AuditSearchData;
import org.obsplatform.commands.service.AuditReadPlatformService;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiParameterHelper;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 *this api class use to perform  different maker actions are approve or delete or reject
 */
@Path("/makercheckers")
@Component
@Scope("singleton")
public class MakercheckersApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName", "groupName", "clientName"));

    private final AuditReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @Autowired
    public MakercheckersApiResource(final AuditReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit,
            final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioCommandSourceWritePlatformService writePlatformService) {
        this.readPlatformService = readPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializerAudit = toApiJsonSerializerAudit;
        this.toApiJsonSerializerSearchTemplate = toApiJsonSerializerSearchTemplate;
        this.writePlatformService = writePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCommands(@Context final UriInfo uriInfo, @QueryParam("actionName") final String actionName,
            @QueryParam("entityName") final String entityName, @QueryParam("resourceId") final Long resourceId,
            @QueryParam("makerId") final Long makerId, @QueryParam("makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") final String makerDateTimeTo, @QueryParam("officeId") final Integer officeId,
            @QueryParam("groupId") final Integer groupId, @QueryParam("clientId") final Integer clientId) {

        final String extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo, officeId, groupId, clientId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final Collection<AuditData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria,settings.isIncludeJson());

        return this.toApiJsonSerializerAudit.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.readPlatformService.retrieveSearchTemplate("makerchecker");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<String>(Arrays.asList("appUsers", "actionNames", "entityNames"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    @POST
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String approveMakerCheckerEntry(@PathParam("auditId") final Long auditId, @QueryParam("command") final String commandParam) {

        CommandProcessingResult result = null;
        if (checkStatus(commandParam, "approve")) {
            result = this.writePlatformService.approveEntry(auditId);
        }else if (checkStatus(commandParam, "reject")) {
            final Long id = this.writePlatformService.rejectEntry(auditId);
            result = CommandProcessingResult.commandOnlyResult(id);
        }else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.toApiJsonSerializerAudit.serialize(result);
    }

    private boolean checkStatus(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteMakerCheckerEntry(@PathParam("auditId") final Long auditId) {

        final Long id = this.writePlatformService.deleteEntry(auditId);

        return this.toApiJsonSerializerAudit.serialize(CommandProcessingResult.commandOnlyResult(id));
    }

    private String getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Integer officeId, final Integer groupId,
            final Integer clientId) {

        String extraCriteria = "";

        if (actionName != null) {
            extraCriteria += " and aud.action_name = " + ApiParameterHelper.sqlEncodeString(actionName);
        }
        if (entityName != null) {
            extraCriteria += " and aud.entity_name like " + ApiParameterHelper.sqlEncodeString(entityName + "%");
        }

        if (resourceId != null) {
            extraCriteria += " and aud.resource_id = " + resourceId;
        }
        if (makerId != null) {
            extraCriteria += " and aud.maker_id = " + makerId;
        }
        if (makerDateTimeFrom != null) {
            extraCriteria += " and DATE_FORMAT(aud.made_on_date,'%y-%m-%d') >= DATE_FORMAT("+ApiParameterHelper.sqlEncodeString(makerDateTimeFrom)+",'%y-%m-%d')";
        }
        if (makerDateTimeTo != null) {
            extraCriteria += " and DATE_FORMAT(aud.made_on_date,'%y-%m-%d') <= DATE_FORMAT("+ApiParameterHelper.sqlEncodeString(makerDateTimeTo)+",'%y-%m-%d')";
        }

        if (officeId != null) {
            extraCriteria += " and aud.office_id = " + officeId;
        }

        if (groupId != null) {
            extraCriteria += " and aud.group_id = " + groupId;
        }

        if (clientId != null) {
            extraCriteria += " and aud.client_id = " + clientId;
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }
}