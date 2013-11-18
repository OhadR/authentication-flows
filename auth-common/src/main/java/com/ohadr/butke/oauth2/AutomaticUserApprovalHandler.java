package com.ohadr.butke.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.stereotype.Component;

@Component
public class AutomaticUserApprovalHandler implements UserApprovalHandler
{
	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication)
	{
		return userAuthentication.isAuthenticated();
	}
}
