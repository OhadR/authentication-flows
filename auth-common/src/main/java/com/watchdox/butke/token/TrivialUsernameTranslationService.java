package com.watchdox.butke.token;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.watchdox.butke.token.interfaces.UsernameTranslationService;

@Component
public class TrivialUsernameTranslationService implements UsernameTranslationService
{
	@Override
	public String getUsernameFromAuthentication(Authentication authentication)
	{
		return authentication.getName();
	}
}
