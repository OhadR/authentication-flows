package com.ohadr.butke.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthProperties
{
	@Value("${com.ohadr.oauth2.token.issuer}")
	private String tokenIssuer;

	@Value("${com.ohadr.oauth2.token.timeToLive}")
	private int tokenTimeToLive;

	@Value("${com.ohadr.oauth2.token.refreshTimeToLive}")
	private int refreshTokenTimeToLive;

	@Value("${com.ohadr.oauth2.apiServer}")
	private String apiServer;
	
	public String getTokenIssuer()
	{
		return tokenIssuer;
	}

	public void setTokenIssuer(String tokenIssuer)
	{
		this.tokenIssuer = tokenIssuer;
	}

	public int getTokenTimeToLive()
	{
		return tokenTimeToLive;
	}

	public void setTokenTimeToLive(int tokenTimeToLive)
	{
		this.tokenTimeToLive = tokenTimeToLive;
	}

	public int getRefreshTokenTimeToLive()
	{
		return refreshTokenTimeToLive;
	}

	public void setRefreshTokenTimeToLive(int refreshTokenTimeToLive)
	{
		this.refreshTokenTimeToLive = refreshTokenTimeToLive;
	}

	public String getApiServer()
	{
		return apiServer;
	}

	public void setApiServer(String apiServer)
	{
		this.apiServer = apiServer;
	}
	
	
}
