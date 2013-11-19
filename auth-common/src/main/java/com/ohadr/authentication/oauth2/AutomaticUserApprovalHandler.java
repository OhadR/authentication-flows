package com.ohadr.authentication.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.stereotype.Component;

@Component
public class AutomaticUserApprovalHandler extends DefaultUserApprovalHandler
{
	//simpler implementation (without the "flag")
	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication)
	{
		return userAuthentication.isAuthenticated();
	}
}
