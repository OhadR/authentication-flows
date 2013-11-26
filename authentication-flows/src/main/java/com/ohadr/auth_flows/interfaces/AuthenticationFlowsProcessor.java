package com.ohadr.auth_flows.interfaces;

import org.apache.commons.lang3.tuple.Pair;

import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


/**
 * manages the data like user's account lock, or number of login-retries, etc.
 * @author OhadR
 *
 */
public interface AuthenticationFlowsProcessor
{
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
			String encodedPassword
//			,String secretQuestion,		NOT IMPLEMENTED
//			String encodedAnswer,		NOT IMPLEMENTED
//			String redirectUri			NOT IMPLEMENTED
			);

	/**
	 * 
	 * @param username
	 * @return boolean, passChangeRequired. true if change password is required.
	 */
	public boolean setLoginSuccessForUser(String username);


	public AuthenticationPolicy getAuthenticationSettings();

	public AccountState isAccountLocked(String email);

	public void sendPasswordRestoreMail(String email);

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

	/**
	 * NOT IMPLEMENTED:
	 * @param email
	 * @return
	 * 
	public String getSecretQuestion(String email);
		 */

}
