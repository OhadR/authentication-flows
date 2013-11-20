package com.ohadr.security.oauth.examples.web;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class OAuth2PreAuthenticationProvider implements AuthenticationProvider
{

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		if (!supports(authentication.getClass()))
		{
			return null;
		}

		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return OAuth2PreAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
