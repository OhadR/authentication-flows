package com.ohadr.auth_flows.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthFlowsProperties
{
	/**
	 * the time in seconds of how long the links that the oauth-srv generates are valid. relevant for the "forgot password" flow, 
	 * where the oauth-srv makes the expiration check. (for other flows, createUser and activateAccount - checks are in the webapp)
	 */
	@Value("${com.ohadr.auth-flows.linksExpirationMinutes}")
	private int linksExpirationMinutes;

	@Value("${com.ohadr.auth-flows.maxAttempts}")
	private int maxAttempts;
	
	@Value("${com.ohadr.auth-flows.isREST}")
	private boolean isREST;
	
	@Value("${com.ohadr.auth-flows.endpoints.accountActivatedEndpointUrl}")
	private String accountActivatedEndpointUrl;
	
	
	public int getLinksExpirationMinutes()
	{
		return linksExpirationMinutes;
	}

	public void setLinksExpirationMinutes(int linksExpirationMinutes)
	{
		this.linksExpirationMinutes = linksExpirationMinutes;
	}
	
	public int getMaxAttempts()
	{
		return maxAttempts;
	}

	public boolean isREST() 
	{
		return isREST;
	}
	
	public String getAccountActivatedEndpointUrl()
	{
		return accountActivatedEndpointUrl;
	}

}
