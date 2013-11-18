package com.ohadr.authentication.token;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ohadr.authentication.token.interfaces.UsernameTranslationService;


@Component
public class TrivialUsernameTranslationService implements UsernameTranslationService
{
	@Override
	public String getUsernameFromAuthentication(Authentication authentication)
	{
		return authentication.getName();
	}
}
