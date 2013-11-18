package com.ohadr.butke.token;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ohadr.butke.config.AuthProperties;
import com.ohadr.butke.token.interfaces.UsernameTranslationService;

@Component("watchdoxAuthorizationServerTokenServices")
public class OAuth2TokenServices implements AuthorizationServerTokenServices, InitializingBean
{
    private static final Logger log = Logger.getLogger(OAuth2TokenServices.class);
    private final static String TOKEN_TYPE = "Bearer";

	@Autowired
	private AuthProperties authProperties;

	@Autowired
	private SignedTokenGenerator signedTokenGenerator;

	private TokenStore tokenStore 
		= new InMemoryTokenStore();

	@Autowired
	private UsernameTranslationService usernameTranslationService;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Assert.notNull(tokenStore, "tokenStore must be set");
		Assert.notNull(usernameTranslationService, "usernameTranslationService must be set");
	}

	@Override
	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException
	{
		log.info("authentication-provider is creating token");
		
		int secondsToExpire = authProperties.getTokenTimeToLive();
		String issuerName = authProperties.getTokenIssuer();
		try
		{
			// TODO figure out how we extract device id & public key parameters
			String email = usernameTranslationService.getUsernameFromAuthentication(authentication);
			String generatedToken = signedTokenGenerator.generateToken(email, issuerName, null, null, secondsToExpire);
			log.info("the generated token: " + generatedToken);
			DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken( generatedToken );
			result.setExpiration(new Date(new Date().getTime() + secondsToExpire * 1000));
			result.setTokenType(TOKEN_TYPE);
			result.setRefreshToken(createRefreshToken(authentication));
			return result;
		}
		catch (Exception e)
		{
			throw new InvalidTokenException("Token generation failed", e);
		}
	}

	@Override
	public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, Set<String> scope) throws AuthenticationException
	{
		log.info("authentication-provider is refreshing token");
		
		// Note: scope is ignored by the current implementation
		// - uri, Mar 14, 2012

		ExpiringOAuth2RefreshToken refreshToken = (ExpiringOAuth2RefreshToken) tokenStore.readRefreshToken(refreshTokenValue);
		if (refreshToken == null)
		{
			throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
		}
		else if (isExpired(refreshToken))
		{
			tokenStore.removeRefreshToken(refreshToken);
			throw new InvalidGrantException("Invalid refresh token: " + refreshToken);
		}

		OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);

		tokenStore.removeRefreshToken(refreshToken);

		return createAccessToken(authentication);
	}

	private ExpiringOAuth2RefreshToken createRefreshToken(OAuth2Authentication authentication)
	{
		int secondsToExpire = authProperties.getRefreshTokenTimeToLive();
		Date expiration = new Date(System.currentTimeMillis() + secondsToExpire * 1000L);
		String refreshTokenValue = UUID.randomUUID().toString();
		ExpiringOAuth2RefreshToken refreshToken = new DefaultExpiringOAuth2RefreshToken(refreshTokenValue, expiration);
		tokenStore.storeRefreshToken(refreshToken, authentication);
		return refreshToken;
	}

	protected boolean isExpired(ExpiringOAuth2RefreshToken refreshToken)
	{
		return refreshToken.getExpiration() == null
		    || System.currentTimeMillis() > refreshToken.getExpiration().getTime();
	}

	public void setTokenStore(TokenStore tokenStore)
	{
		this.tokenStore = tokenStore;
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication)
	{
		return createAccessToken(authentication);
	}

	public void setUsernameTranslationService(UsernameTranslationService usernameTranslationService)
	{
		this.usernameTranslationService = usernameTranslationService;
	}

	public UsernameTranslationService getUsernameTranslationService()
	{
		return usernameTranslationService;
	}
}
