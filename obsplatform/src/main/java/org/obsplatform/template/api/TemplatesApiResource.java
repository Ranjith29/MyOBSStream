/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.template.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.template.data.TemplateData;
import org.obsplatform.template.domain.Template;
import org.obsplatform.template.domain.TemplateEntity;
import org.obsplatform.template.domain.TemplateType;
import org.obsplatform.template.service.TemplateMergeService;
import org.obsplatform.template.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/templates")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Scope("singleton")
public class TemplatesApiResource {

    private final Set<String> RESPONSE_TEMPLATES_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id"));
    private final Set<String> RESPONSE_TEMPLATE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "entities", "types", "template"));
    private final String RESOURCE_NAME_FOR_PERMISSION = "template";

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<Template> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<TemplateData> templateDataApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final TemplateService templateService;
    private final TemplateMergeService templateMergeService;
    private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

    @Autowired
    public TemplatesApiResource(final PlatformSecurityContext context, final DefaultToApiJsonSerializer<Template> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<TemplateData> templateDataApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final TemplateService templateService,
            final TemplateMergeService templateMergeService,
            final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.templateDataApiJsonSerializer = templateDataApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.templateService = templateService;
        this.templateMergeService = templateMergeService;
        this.commandSourceWritePlatformService = commandSourceWritePlatformService;
    }

    @GET
    public String retrieveAll(@DefaultValue("-1") @QueryParam("typeId") final int typeId,
            @DefaultValue("-1") @QueryParam("entityId") final int entityId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_NAME_FOR_PERMISSION);

        // FIXME - we dont use the ORM when doing fetches - we write SQL and
        // fetch through JDBC returning data to be serialized to JSON
        List<Template> templates = new ArrayList<>();

        if (typeId != -1 && entityId != -1) {
            templates = this.templateService.getAllByEntityAndType(TemplateEntity.values()[entityId], TemplateType.values()[typeId]);
        } else {
            templates = this.templateService.getAll();
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, templates, this.RESPONSE_TEMPLATES_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    public String template(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_NAME_FOR_PERMISSION);
        final TemplateData templateData = TemplateData.template();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.templateDataApiJsonSerializer.serialize(settings, templateData, this.RESPONSE_TEMPLATES_DATA_PARAMETERS);
    }

    @POST
    public String createTemplate(final String apiRequestBodyAsJson) {
    	
    	 this.context.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTemplate().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{templateId}")
    public String retrieveOne(@PathParam("templateId") final Long templateId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_NAME_FOR_PERMISSION);

        final Template template = this.templateService.findOneById(templateId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, template, this.RESPONSE_TEMPLATES_DATA_PARAMETERS);
    }

    @GET
    @Path("{templateId}/template")
    public String getTemplateByTemplate(@PathParam("templateId") final Long templateId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.RESOURCE_NAME_FOR_PERMISSION);

        final TemplateData template = TemplateData.template(this.templateService.findOneById(templateId));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.templateDataApiJsonSerializer.serialize(settings, template, this.RESPONSE_TEMPLATE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{templateId}")
    public String saveTemplate(@PathParam("templateId") final Long templateId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTemplate(templateId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{templateId}")
    public String deleteTemplate(@PathParam("templateId") final Long templateId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteTemplate(templateId).build();

        final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{templateId}")
    @Produces({ MediaType.TEXT_HTML })
    public String mergeTemplate(@PathParam("templateId") final Long templateId, @Context final UriInfo uriInfo,
            final String apiRequestBodyAsJson) throws MalformedURLException, IOException {

        final Template template = this.templateService.findOneById(templateId);

        @SuppressWarnings("unchecked")
        final HashMap<String, Object> result = new ObjectMapper().readValue(apiRequestBodyAsJson, HashMap.class);

        final MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();	
        final Map<String, Object> parametersMap = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {

            if (entry.getValue().size() == 1) {
                parametersMap.put(entry.getKey(), entry.getValue().get(0));
            } else {
                parametersMap.put(entry.getKey(), entry.getValue());
            }
        }

        parametersMap.put("BASE_URI", uriInfo.getBaseUri());
        parametersMap.putAll(result);
        return this.templateMergeService.compile(template, parametersMap);
    }
}