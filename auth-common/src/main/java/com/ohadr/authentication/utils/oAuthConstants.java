package com.ohadr.authentication.utils;

import org.springframework.stereotype.Component;

@Component
public class oAuthConstants
{
	public static final String OK = "OK";
	public static final String ERROR = "ERROR";

	public static final String OAUTH_WEB_APP_NAME = "oauth-srv";
	public static final String ENCRYPTED_USERNAME_PARAM_NAME = "e";

	public static final String HASH_PARAM_NAME = "enc";
	public static final String REDIRECT_URI_PARAM_NAME = "redirect_uri";


}
