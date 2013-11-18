package com.ohadr.security.oauth.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ClientProperties
{
	@Value("${oauth2.client.id}")
	private String clientId;

	@Value("${oauth2.client.secret}")
	private String clientSecret;

	@Value("${oauth2.client.autoHashSecret}")
	private boolean autoHashSecret;

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getClientSecret()
	{
		return clientSecret;
	}

	public void setClientSecret(String clientSecret)
	{
		this.clientSecret = clientSecret;
	}

	public boolean isAutoHashSecret()
	{
		return autoHashSecret;
	}

	public void setAutoHashSecret(boolean autoHashSecret)
	{
		this.autoHashSecret = autoHashSecret;
	}
}
