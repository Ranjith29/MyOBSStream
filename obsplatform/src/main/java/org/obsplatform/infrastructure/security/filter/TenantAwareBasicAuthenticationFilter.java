/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.StopWatch;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.cache.domain.CacheType;
import org.obsplatform.infrastructure.cache.service.CacheWritePlatformService;
import org.obsplatform.infrastructure.configuration.data.LicenseData;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.obsplatform.infrastructure.configuration.exception.InvalidLicenseKeyException;
import org.obsplatform.infrastructure.configuration.service.LicenseUpdateService;
import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.infrastructure.security.data.PlatformRequestLog;
import org.obsplatform.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.obsplatform.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Service;

/**
 * A customised version of spring security's {@link BasicAuthenticationFilter}.
 * 
 * This filter is responsible for extracting multi-tenant and basic auth
 * credentials from the request and checking that the details provided are
 * valid.
 * 
 * If multi-tenant and basic auth credentials are valid, the details of the
 * tenant are stored in {@link ObsPlatformTenant} and stored in a
 * {@link ThreadLocal} variable for this request using
 * {@link ThreadLocalContextUtil}.
 * 
 * If multi-tenant and basic auth credentials are invalid, a http error response
 * is returned.
 */
@Service(value = "basicAuthenticationProcessingFilter")
@Profile("basicauth")
public class TenantAwareBasicAuthenticationFilter extends BasicAuthenticationFilter {

	private static boolean firstRequestProcessed = false;
	private final static Logger logger = LoggerFactory.getLogger(TenantAwareBasicAuthenticationFilter.class);

	// ashok changed
	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
	private RememberMeServices rememberMeServices = new NullRememberMeServices();

	 private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
	 private final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;
	 private final ConfigurationDomainService configurationDomainService;
	 private final CacheWritePlatformService cacheWritePlatformService;
	 private final LicenseUpdateService licenseUpdateService;
	 
	 @Autowired
	 private AuthenticationManager authenticationManager;
	 
	 @Autowired
	 private  AuthenticationEntryPoint authenticationEntryPoint;

	 private final String tenantRequestHeader = "X-Obs-Platform-TenantId";
	 private final boolean exceptionIfHeaderMissing = true;
	 private final String accessToken = "accessToken";
     private final String credentialsCharset = "UTF-8";
     private final String tenantIdentifier = "tenantIdentifier";
     private final String defaultCode = "default";
	 private final String GET = "GET";
	 private final String POST = "POST";

	    @Autowired
	    public TenantAwareBasicAuthenticationFilter(final AuthenticationManager authenticationManager,
	    		final AuthenticationEntryPoint authenticationEntryPoint,
	    		final BasicAuthTenantDetailsService basicAuthTenantDetailsService,
	            final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer, 
	            final ConfigurationDomainService configurationDomainService,
	            final CacheWritePlatformService cacheWritePlatformService,
	            final LicenseUpdateService licenseUpdateService) {
	    	
	        super(authenticationManager, authenticationEntryPoint);
	        
	        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
	        this.toApiJsonSerializer = toApiJsonSerializer;
	        this.configurationDomainService = configurationDomainService;
	        this.cacheWritePlatformService = cacheWritePlatformService;
	        this.licenseUpdateService = licenseUpdateService;
	    }
	

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {

		ObsPlatformTenant tenant;
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String path = request.getRequestURI();

		StopWatch task = new StopWatch();
		task.start();
		
		try {

			if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
				// ignore to allow 'preflight' requests from AJAX applications in different origin (domain name)
				super.doFilter(req, res, chain);

			} else if ((path.contains("/api/v1/paymentgateways") && request.getMethod().equalsIgnoreCase(POST))
					|| (path.contains("/api/v1/entitlements/getuser") && request.getMethod().equalsIgnoreCase(GET))
					|| (path.contains("/api/v1/recurringpayments/authorize/silentpost") && request.getMethod().equalsIgnoreCase(POST))) {

				String username = request.getParameter("username");
				String password = request.getParameter("password");

				tenant = getTenantIdentifier(request);

				boolean isValid = this.licenseUpdateService.checkIfKeyIsValid(tenant.getLicensekey(), tenant);
				if (!isValid) {
					throw new InvalidLicenseKeyException("License key Exipired.");
				}
				ThreadLocalContextUtil.setTenant(tenant);

				authenticateLocal(request, chain, response, username, password);

			} else if (path.contains("/api/v1/entitlements/getauth") && request.getMethod().equalsIgnoreCase(GET)) {

				tenant = getTenantIdentifier(request);

				boolean isValid = this.licenseUpdateService.checkIfKeyIsValid(tenant.getLicensekey(), tenant);
				if (!isValid) {
					throw new InvalidLicenseKeyException("License key Exipired.");
				}
				ThreadLocalContextUtil.setTenant(tenant);
				super.doFilter(req, res, chain);

			} else if (path.contains("/api/v1/keyinfo")) {

				tenant = getTenantIdentifier(request);

				LicenseData licenseData = this.licenseUpdateService.getLicenseDetails(tenant.getLicensekey());
				PrintWriter printWriter = res.getWriter();
				printWriter.print(new LocalDate(licenseData.getKeyDate()));

			} else if (path.contains("/api/v1/licensekey")) {

				tenant = getTenantIdentifier(request);

				this.licenseUpdateService.updateLicenseKey(req, tenant);
				
			} else if (((path.contains("/api/v1/billmaster")) || (path.contains("/api/v1/runreports"))  || (path.contains("/api/v1/clients"))) && request.getMethod().equalsIgnoreCase(GET) 
					&& request.getParameterMap().containsKey(accessToken)) {

				tenant = getTenantIdentifier(request);
				
				String accessTokenVal = new String(Base64.decode(request.getParameter(accessToken).getBytes(credentialsCharset)));
				String[] credentials = accessTokenVal.split(":");
				String username = credentials[0];
				String password = credentials[1];

				authenticateLocal(request, chain, response, username, password);
			
			} else {

				 String tenantIdentifier = request.getHeader(this.tenantRequestHeader);

				 if (org.apache.commons.lang.StringUtils.isBlank(tenantIdentifier)) {
	                    tenantIdentifier = request.getParameter("tenantIdentifier");
	               }

				 if (tenantIdentifier == null && this.exceptionIfHeaderMissing) { throw new InvalidTenantIdentiferException(
	                        "No tenant identifier found: Add request header of '" + this.tenantRequestHeader
	                                + "' or add the parameter 'tenantIdentifier' to query string of request URL."); }

	                String pathInfo = request.getRequestURI();
	                boolean isReportRequest = false;
	                if (pathInfo != null && pathInfo.contains("report")) {
	                    isReportRequest = true;
	                }

	              // check tenants database for tenantIdentifier
				  tenant = this.basicAuthTenantDetailsService.loadTenantById(tenantIdentifier, isReportRequest);

	                ThreadLocalContextUtil.setTenant(tenant);
	                String authToken = request.getHeader("Authorization");
	                
	                boolean isValid = this.licenseUpdateService.checkIfKeyIsValid(tenant.getLicensekey(), tenant);
					if (!isValid) {
						throw new InvalidLicenseKeyException("License key Exipired.");
					}

	                if (authToken != null && authToken.startsWith("Basic ")) {
	                    ThreadLocalContextUtil.setAuthToken(authToken.replaceFirst("Basic ", ""));
	                }

	                if (!firstRequestProcessed) {
	                    final String baseUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "/");
	                    System.setProperty("baseUrl", baseUrl);

	                    final boolean ehcacheEnabled = this.configurationDomainService.isEhcacheEnabled();
	                    if (ehcacheEnabled) {
	                        this.cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
	                    } else {
	                        this.cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
	                    }
	                    TenantAwareBasicAuthenticationFilter.firstRequestProcessed = true;
	                }
	            }

			super.doFilter(req, res, chain);
		} catch (final InvalidTenantIdentiferException e) {
			// deal with exception at low level
			SecurityContextHolder.getContext().setAuthentication(null);
			response.addHeader("WWW-Authenticate", "Basic realm=\""+ "Obs Platform API" + "\"");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
			
		} catch (AuthenticationException failed) {
			
			SecurityContextHolder.clearContext();
			rememberMeServices.loginFail(request, response);
			onUnsuccessfulAuthentication(request, response, failed);
			authenticationEntryPoint.commence(request, response, failed);
			return;

		} finally {
			task.stop();
			final PlatformRequestLog log = PlatformRequestLog.from(task,request);
			logger.info(this.toApiJsonSerializer.serialize(log));
		}
	}

	private final ObsPlatformTenant getTenantIdentifier(final HttpServletRequest request) {

		if (request.getParameterMap().containsKey(tenantIdentifier)) {
			return this.basicAuthTenantDetailsService.loadTenantById(request.getParameter(tenantIdentifier), false);// isReport
		} else {
			return this.basicAuthTenantDetailsService.loadTenantById(defaultCode, false);
		}
	}

	private final void authenticateLocal(HttpServletRequest request,FilterChain chain, HttpServletResponse response, String username,
			String password) throws IOException, ServletException {

		if (!(org.apache.commons.lang.StringUtils.isBlank(username) || org.apache.commons.lang.StringUtils.isBlank(password))) {

			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
			authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
			Authentication authResult = authenticationManager.authenticate(authRequest);

			SecurityContextHolder.getContext().setAuthentication(authResult);

			rememberMeServices.loginSuccess(request, response, authResult);

			onSuccessfulAuthentication(request, response, authResult);

			chain.doFilter(request, response);

		} else {
			throw new AuthenticationCredentialsNotFoundException("Credentials are not valid");
		}
	}
	
	  /*@Override
	    protected void onSuccessfulAuthentication(HttpServletRequest request,
	    		HttpServletResponse response, Authentication authResult)
	    		throws IOException {
	    	super.onSuccessfulAuthentication(request, response, authResult);
			AppUser user = (AppUser) authResult.getPrincipal();
			
			String pathURL = request.getRequestURI();
			boolean isSelfServiceRequest = (pathURL != null && pathURL.contains("/self/"));

			boolean notAllowed = ((isSelfServiceRequest && !user.isSelfServiceUser())
					||(!isSelfServiceRequest && user.isSelfServiceUser()));
			
			if(notAllowed){
				throw new BadCredentialsException("User not authorised to use the requested resource.");
			}
	    }*/
}