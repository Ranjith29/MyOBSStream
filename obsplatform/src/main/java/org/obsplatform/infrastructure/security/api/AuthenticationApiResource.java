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
import javax.ws.rs.POST;
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
import org.obsplatform.infrastructure.security.data.AuthenticatedUserData;
import org.obsplatform.useradministration.data.RoleData;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.util.Base64;

@Path("/authentication")
@Component
@Profile("basicauth")
@Scope("singleton")
public class AuthenticationApiResource {

    private final DaoAuthenticationProvider customAuthenticationProvider;
    private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService;
    private final LicenseUpdateService licenseUpdateService; 
    private final UserChatReadplatformReadService userChatReadplatformReadService;
    private final LoginHistoryRepository loginHistoryRepository;
    
    @Autowired
    public AuthenticationApiResource( @Qualifier("customAuthenticationProvider") final DaoAuthenticationProvider customAuthenticationProvider,
            final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService, 
            final  UserChatReadplatformReadService userChatReadplatformReadService,
            final LoginHistoryRepository loginHistoryRepository,
            final LicenseUpdateService licenseUpdateService) {
    	
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.licenseUpdateService = licenseUpdateService;
        this.userChatReadplatformReadService=userChatReadplatformReadService;
        this.loginHistoryRepository=loginHistoryRepository;
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String authenticate(@QueryParam("username") final String username, @QueryParam("password") final String password,@Context HttpServletRequest req) {
    	
		final Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
		final Authentication authenticationCheck = this.customAuthenticationProvider.authenticate(authentication);

		String notificationMessage = null;
		final LicenseData licenseData = this.licenseUpdateService.getLicenseDetails(ThreadLocalContextUtil.getTenant().getLicensekey());
		int days = Days.daysBetween(DateUtils.getLocalDateOfTenant(),new LocalDate(licenseData.getKeyDate())).getDays();

		if (days < 7) {
			notificationMessage = "License will be exipired on "+ new SimpleDateFormat("dd-MMM-yyyy").format(licenseData.getKeyDate()) + ". Please Update";
		}

        final Collection<String> permissions = new ArrayList<>();
        AuthenticatedUserData authenticatedUserData = new AuthenticatedUserData(username, permissions);
      
        if (authenticationCheck.isAuthenticated()) {
        	
			final String ipAddress = req.getRemoteHost();/** Returns IpAddress of user */
			final String session = req.getSession().getId();/** creates session and returns sessionId */
			int maxTime = req.getSession().getMaxInactiveInterval();
			Long loginHistoryId=0L;
			/**
			 * Condition to Login History Calls When Session is New One
			 * @author rakesh
			 * */
			if (req.getSession().isNew() && !username.equalsIgnoreCase("selfcare")) {
				LoginHistory loginHistory = new LoginHistory(ipAddress, null,session, DateUtils.getDateOfTenant(), null, username,"ACTIVE");
				this.loginHistoryRepository.save(loginHistory);
				loginHistoryId = loginHistory.getId();
				req.getSession().setAttribute("lId", loginHistoryId);
			}
            
			final Collection<GrantedAuthority> authorities = new ArrayList<>(authenticationCheck.getAuthorities());
            for (final GrantedAuthority grantedAuthority : authorities) {
                permissions.add(grantedAuthority.getAuthority());
            }
            
            final byte[] base64EncodedAuthenticationKey = Base64.encode(username + ":" + password);

            final AppUser principal = (AppUser) authenticationCheck.getPrincipal();
            final Collection<RoleData> roles = new ArrayList<>();
            final Set<Role> userRoles = principal.getRoles();
            for (final Role role : userRoles) {
                roles.add(role.toData());
            }
            //Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();
            Long unreadMessages=this.userChatReadplatformReadService.getUnreadMessages(username);

            authenticatedUserData = new AuthenticatedUserData(username, roles, permissions, principal.getId(), new String(base64EncodedAuthenticationKey),
            		unreadMessages,ipAddress,session,maxTime,loginHistoryId,notificationMessage);
        }

        return this.apiJsonSerializerService.serialize(authenticatedUserData);
    }
}