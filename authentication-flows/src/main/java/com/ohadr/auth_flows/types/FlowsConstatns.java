package com.ohadr.auth_flows.types;

public abstract class FlowsConstatns 
{
	public static final String OK = "OK";
	public static final String ERROR = "ERROR";

	public static final String OAUTH_WEB_APP_NAME = "oauth-srv";
	public static final String ENCRYPTED_USERNAME_PARAM_NAME = "e";

	public static final String HASH_PARAM_NAME = "enc";
	public static final String REDIRECT_URI_PARAM_NAME = "redirect_uri";
	public static final int DAY_IN_MILLI = 24 * 60 * 60 * 1000;

	
	
	public static final String ACTIVATE_ACCOUNT_ENDPOINT = "/aa";
	public static final String RESTORE_PASSWORD_ENDPOINT = "/rp";
	
	public class MailMessage
	{
		public static final String AUTHENTICATION_MAIL_SUBJECT = "OhadR Authentication Service: Account Created Successfully";
		public static final String AUTHENTICATION_MAIL_BODY = "Account Created Successfully. bla bla bla... please click on this link to activate your account:";

		public static final String RESTORE_PASSWORD_MAIL_SUBJECT = "OhadR Authentication Service: Password Restore Request";
		public static final String RESTORE_PASSWORD_MAIL_BODY = "you forgot your password and bla bla bla... please click on this link:";
	}

}
