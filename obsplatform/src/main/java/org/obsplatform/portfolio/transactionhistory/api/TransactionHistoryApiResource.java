package org.obsplatform.portfolio.transactionhistory.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.transactionhistory.data.TransactionHistoryData;
import org.obsplatform.portfolio.transactionhistory.service.TransactionHistoryReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("transactionhistory")
@Component
@Scope("singleton")
public class TransactionHistoryApiResource {

	
	private String resourceType = "TRANSACTIONHISTORY";
	private PlatformSecurityContext context;
	private TransactionHistoryReadPlatformService transactionHistoryReadPlatformService;
	private DefaultToApiJsonSerializer<TransactionHistoryData> apiJsonSerializer;
	
	@Autowired
	public TransactionHistoryApiResource(final PlatformSecurityContext context,
			final TransactionHistoryReadPlatformService transactionHistoryReadPlatformService,
			final DefaultToApiJsonSerializer<TransactionHistoryData> apiJsonSerializer) {

		this.context = context;
		this.transactionHistoryReadPlatformService = transactionHistoryReadPlatformService;
		this.apiJsonSerializer = apiJsonSerializer;
	}
	
	@GET
	@Path("{clientId}")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String retriveTransactionHistoryByClientId(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo,
			@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset){
	
		context.authenticatedUser().validateHasReadPermission(resourceType);
		final SearchSqlQuery searchTransactionHistory =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<TransactionHistoryData> transactionHistory = transactionHistoryReadPlatformService.retriveTransactionHistoryClientId(searchTransactionHistory,clientId);
		return apiJsonSerializer.serialize(transactionHistory);
	}
	

	@GET
	@Path("template/{clientId}")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String retriveTransactionHistoryById(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo,
			@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset){
		
		context.authenticatedUser().validateHasReadPermission(resourceType);
		final SearchSqlQuery searchTransactionHistory =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<TransactionHistoryData> transactionHistory = transactionHistoryReadPlatformService.retriveTransactionHistoryById(searchTransactionHistory,clientId);
		return apiJsonSerializer.serialize(transactionHistory);
	}
	
}
