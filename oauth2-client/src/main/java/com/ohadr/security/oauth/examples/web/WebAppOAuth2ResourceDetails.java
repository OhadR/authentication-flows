package com.ohadr.security.oauth.examples.web;

import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

/**
 * this class implements the "hack" - Spring "forces" also the client app to manage its own authentication, 
 * so this class attempts to overcome this limitation (but i failed making it work.... ohad)
 */

public class WebAppOAuth2ResourceDetails extends AuthorizationCodeResourceDetails
{
	public final static String WATCHDOX_OAUTH2_RESOURCE_ID = "www.watchdox.com";

	public WebAppOAuth2ResourceDetails(String clientId, String clientSecret)
	{
		setId(WATCHDOX_OAUTH2_RESOURCE_ID);
		setClientId(clientId);
		if (clientSecret.isEmpty())
		{
			setClientAuthenticationScheme(AuthenticationScheme.header);
			setClientSecret(clientSecret);
		}
	}

	@Override
	public AuthenticationScheme getClientAuthenticationScheme()
	{
		return AuthenticationScheme.form;
	}
	
	/**
	 * the "hack" - in XXX spring checks this, o/w an exception thrown. it is good for cases where the
	 * client does not need any authentication of its own (like in this case)
	 */
	@Override
	public boolean isClientOnly()
	{
		return true;
	}
	
}
