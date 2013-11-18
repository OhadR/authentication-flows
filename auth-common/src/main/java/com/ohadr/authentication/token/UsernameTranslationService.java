package com.ohadr.authentication.token;

import org.springframework.security.core.Authentication;

public interface UsernameTranslationService
{
	public String getUsernameFromAuthentication(Authentication authentication);
}
