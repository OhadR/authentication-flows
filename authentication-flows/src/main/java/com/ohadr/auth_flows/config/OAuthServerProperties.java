package com.ohadr.auth_flows.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuthServerProperties
{
	/**
	 * the time in seconds of how long the links that the oauth-srv generates are valid. relevant for the "forgot password" flow, 
	 * where the oauth-srv makes the expiration check. (for other flows, createUser and activateAccount - checks are in the webapp)
	 */
	@Value("${com.ohadr.oauth2.linksExpirationMinutes}")
	private int linksExpirationMinutes;


	
	
	public int getLinksExpirationMinutes()
	{
		return linksExpirationMinutes;
	}

	public void setLinksExpirationMinutes(int linksExpirationMinutes)
	{
		this.linksExpirationMinutes = linksExpirationMinutes;
	}

}
