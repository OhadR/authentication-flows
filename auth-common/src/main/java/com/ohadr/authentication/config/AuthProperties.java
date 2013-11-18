package com.ohadr.authentication.config;

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

}
