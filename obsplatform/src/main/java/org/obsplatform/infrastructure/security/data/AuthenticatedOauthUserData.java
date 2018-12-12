/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.security.data;

import java.util.Collection;

import org.obsplatform.useradministration.data.RoleData;

/**
 * Immutable data object for authentication. Used in case of Oauth2.
 */
public class AuthenticatedOauthUserData {

    @SuppressWarnings("unused")
    private final String username;
    @SuppressWarnings("unused")
    private final Long userId;
    @SuppressWarnings("unused")
    private final String accessToken;
    @SuppressWarnings("unused")
    private final boolean authenticated;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final Long staffId;
    @SuppressWarnings("unused")
    private final String staffDisplayName;
    @SuppressWarnings("unused")
    private final Collection<RoleData> roles;
    @SuppressWarnings("unused")
    private final Collection<String> permissions;
    @SuppressWarnings("unused")
    private final boolean shouldRenewPassword;
    
    @SuppressWarnings("unused")
    private final Long unReadMessages;
    @SuppressWarnings("unused")
    private final String ipAddress;
    @SuppressWarnings("unused")
    private final String session;
    @SuppressWarnings("unused")
    private final Integer maxTime;
    @SuppressWarnings("unused")
    private final Long loginHistoryId;
    @SuppressWarnings("unused")
	private final String notificationMessage;
  

    public AuthenticatedOauthUserData(final String username, final Collection<String> permissions) {
       
    	this.username = username;
        this.userId = null;
        this.accessToken = null;
        this.authenticated = false;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.roles = null;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
        this.unReadMessages=null;
        this.ipAddress=null;
        this.session=null;
        this.maxTime=null;
        this.loginHistoryId=null;
        this.notificationMessage = null;
    }

    public AuthenticatedOauthUserData(final String username, final Long officeId, final String officeName, final Long staffId,
            final String staffDisplayName, final Collection<RoleData> roles,final Collection<String> permissions, final Long userId, 
            final String accessToken,final Long unreadMessages,final String remoteHost,final String session, final int maxTime,
            final Long loginHistoryId,final String notificationMessage) {
        
    	this.username = username;
        this.officeId = officeId;
        this.officeName = officeName;
        this.staffId = staffId;
        this.staffDisplayName = staffDisplayName;
        this.userId = userId;
        this.accessToken = accessToken;
        this.authenticated = true;
        this.roles = roles;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
        this.unReadMessages=unreadMessages;
        this.ipAddress = remoteHost;
        this.session = session;
        this.maxTime = maxTime;
        this.loginHistoryId=loginHistoryId;
        this.notificationMessage = notificationMessage;
    }

    public AuthenticatedOauthUserData(final String username, final Long userId, final String accessToken) {
        
    	this.username = username;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.userId = userId;
        this.accessToken = accessToken;
        this.authenticated = true;
        this.roles = null;
        this.permissions = null;
        this.shouldRenewPassword = true;
        this.unReadMessages=null;
        this.ipAddress=null;
        this.session=null;
        this.maxTime=null;
        this.loginHistoryId=null;
        this.notificationMessage = null;
    }
}