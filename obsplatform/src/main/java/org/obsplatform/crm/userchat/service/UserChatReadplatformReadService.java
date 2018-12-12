package org.obsplatform.crm.userchat.service;

import java.util.List;

import org.obsplatform.crm.userchat.data.UserChatData;

public interface UserChatReadplatformReadService {

	List<UserChatData> getUserChatDetails();
	List<UserChatData> getUserSentMessageDetails();
    Long getUnreadMessages(String username);	

}
