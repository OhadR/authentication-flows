package com.ohadr.oauth_srv.interfaces;


/**
 * manages the data like user's account lock, or number of login-retries, etc.
 * @author OhadR
 *
 */
public interface OAuthDataManagement
{
	public boolean setLoginSuccessForUser(String username);
}
