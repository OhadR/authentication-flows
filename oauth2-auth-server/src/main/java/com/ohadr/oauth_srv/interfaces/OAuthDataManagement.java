package com.ohadr.oauth_srv.interfaces;

import org.apache.commons.lang3.tuple.Pair;

import com.ohadr.oauth_srv.types.AuthenticationPolicy;
import com.ohadr.oauth_srv.types.OauthAccountState;


/**
 * manages the data like user's account lock, or number of login-retries, etc.
 * @author OhadR
 *
 */
public interface OAuthDataManagement
{
	/**
	 * 
	 * @param username
	 * @return boolean, passChangeRequired. true if change password is required.
	 */
	public boolean setLoginSuccessForUser(String username);

	/**
	 * 
	 * @param email
	 * @param encodedPassword
	 * @param secretQuestion
	 * @param encodedAnswer
	 * @return pair of strings. left is the status OK | ERROR, right is the message
	 */
	public Pair<String, String> createAccount(
			String email, 
			String encodedPassword,
			String secretQuestion, 
			String encodedAnswer,
			String redirectUri);

	public AuthenticationPolicy getAuthenticationSettings();

	public OauthAccountState isAccountLocked(String email);

	public void sendPasswordRestoreMail(String email, String redirectUri);

	public String getSecretAnswer(String email);

	public boolean setLoginFailureForUser(String email);

	public void sendUnlockAccountMail(String email, String redirectUri);

	public void setPassword(String email, String encodedPassword);

	/**
	 * 
	 * @param email
	 * @param encodedCurrentPassword
	 * @param encodedNewPassword
	 * @return pair of strings. left is the status OK | ERROR, right is the message
	 */
	public Pair<String, String> changePassword(String email, String encodedCurrentPassword,
			String newEncodedPassword);

	public String getSecretQuestion(String email);
}
