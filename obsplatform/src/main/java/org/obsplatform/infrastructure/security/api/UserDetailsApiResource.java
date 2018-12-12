/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.obsplatform.billing.loginhistory.domain.LoginHistory;
import org.obsplatform.billing.loginhistory.domain.LoginHistoryRepository;
import org.obsplatform.crm.userchat.service.UserChatReadplatformReadService;
import org.obsplatform.infrastructure.configuration.data.LicenseData;
import org.obsplatform.infrastructure.configuration.service.LicenseUpdateService;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.infrastructure.security.data.AuthenticatedOauthUserData;
import org.obsplatform.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
import org.obsplatform.useradministration.data.RoleData;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;

/*
 * Implementation of Oauth2 authentication APIs, loaded only when "oauth" profile is enabled. 
 */
@Path("/userdetails")
@Component
@Profile("oauth")
@Scope("singleton")
public class UserDetailsApiResource {

    private final ResourceServerTokenServices tokenServices;
    private final ToApiJsonSerializer<AuthenticatedOauthUserData> apiJsonSerializerService;
    private final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext;
    private final LicenseUpdateService licenseUpdateService; 
    private final UserChatReadplatformReadService userChatReadplatformReadService;
    private final LoginHistoryRepository loginHistoryRepository;

    @Autowired
    public UserDetailsApiResource(@Qualifier("tokenServices") final ResourceServerTokenServices tokenServices,
            final ToApiJsonSerializer<AuthenticatedOauthUserData> apiJsonSerializerService,
            final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext,
            final LicenseUpdateService licenseUpdateService, 
            final UserChatReadplatformReadService userChatReadplatformReadService,
            final LoginHistoryRepository loginHistoryRepository) {
        this.tokenServices = tokenServices;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.springSecurityPlatformSecurityContext = springSecurityPlatformSecurityContext;
        this.licenseUpdateService = licenseUpdateService;
        this.userChatReadplatformReadService=userChatReadplatformReadService;
        this.loginHistoryRepository=loginHistoryRepository;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String fetchAuthenticatedUserData(@QueryParam("access_token") final String accessToken,@Context HttpServletRequest req) {

        final Authentication authentication = this.tokenServices.loadAuthentication(accessToken);
        
    	String notificationMessage = null;
		final LicenseData licenseData = this.licenseUpdateService.getLicenseDetails(ThreadLocalContextUtil.getTenant().getLicensekey());
		int days = Days.daysBetween(DateUtils.getLocalDateOfTenant(),new LocalDate(licenseData.getKeyDate())).getDays();

		if (days < 7) {
			notificationMessage = "License will be exipired on "+ new SimpleDateFormat("dd-MMM-yyyy").format(licenseData.getKeyDate()) + ". Please Update";
		}
        
        if (authentication.isAuthenticated()) {
            final AppUser principal = (AppUser) authentication.getPrincipal();

            final Collection<String> permissions = new ArrayList<>();
            AuthenticatedOauthUserData authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), permissions);

            final Collection<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
            for (final GrantedAuthority grantedAuthority : authorities) {
                permissions.add(grantedAuthority.getAuthority());
            }

            final Collection<RoleData> roles = new ArrayList<>();
            final Set<Role> userRoles = principal.getRoles();
            for (final Role role : userRoles) {
                roles.add(role.toData());
            }

            final Long officeId = principal.getOffice().getId();
            final String officeName = principal.getOffice().getName();
            final String ipAddress = req.getRemoteHost();/** Returns IpAddress of user */
            final String session = req.getSession().getId();/** creates session and returns sessionId */
            int maxTime = req.getSession().getMaxInactiveInterval();
            Long loginHistoryId = 0L;
            
            Long unreadMessages=this.userChatReadplatformReadService.getUnreadMessages(principal.getUsername());
            
			/**
			 * Condition to Login History Calls When Session is New One
			 * @author rakesh
			 * */
			if (req.getSession().isNew() && !principal.getUsername().equalsIgnoreCase("selfcare")) {
				LoginHistory loginHistory = new LoginHistory(ipAddress, null,session, DateUtils.getDateOfTenant(), null, principal.getUsername(),"ACTIVE");
				this.loginHistoryRepository.save(loginHistory);
				loginHistoryId = loginHistory.getId();
				req.getSession().setAttribute("lId", loginHistoryId);
			}

            authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), officeId, officeName, null,null, roles, permissions, 
            		principal.getId(), accessToken,unreadMessages,ipAddress,session,maxTime, loginHistoryId,notificationMessage);

           /* if (this.springSecurityPlatformSecurityContext.doesPasswordHasToBeRenewed(principal)) {
                authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), principal.getId(), accessToken);
            } else {

                authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), officeId, officeName, staffId, staffDisplayName,
                        organisationalRole, roles, permissions, principal.getId(), accessToken);
            }*/
            return this.apiJsonSerializerService.serialize(authenticatedUserData);
        }
        return null;

    }
}