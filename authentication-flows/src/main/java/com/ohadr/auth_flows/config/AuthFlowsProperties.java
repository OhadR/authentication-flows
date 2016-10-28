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

	@Value("${com.ohadr.auth-flows.isREST}")
	private boolean isREST;
	
	@Value("${com.ohadr.auth-flows.endpoints.accountActivatedEndpointUrl}")
	private String accountActivatedEndpointUrl;
	
	@Value("${com.ohadr.auth-flows.endpoints.loginSuccessEndpointUrl}")
	private String loginSuccessEndpointUrl;
	
	@Value("${com.ohadr.auth-flows.email.baseUrlPath}")
	private String baseUrlPath;

	/**
	 * indicates the "from" field of the emails that auth-flows sends.
	 */
	@Value("${com.ohadr.auth-flows.email.fromField}")
	private String authFlowsEmailsFromField;
	
	public int getLinksExpirationMinutes()
	{
		return linksExpirationMinutes;
	}

	public void setLinksExpirationMinutes(int linksExpirationMinutes)
	{
		this.linksExpirationMinutes = linksExpirationMinutes;
	}
	
	public boolean isREST() 
	{
		return isREST;
	}
	
	public String getAccountActivatedEndpointUrl()
	{
		return accountActivatedEndpointUrl;
	}

	public String getLoginSuccessEndpointUrl()
	{
		return loginSuccessEndpointUrl;
	}

	public String getBaseUrlPath()
	{
		return baseUrlPath;
	}	

	public String getAuthFlowsEmailsFromField()
	{
		return authFlowsEmailsFromField;
	}

}
