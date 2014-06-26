package com.ohadr.auth_flows.interfaces;

import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;

import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.web.AuthenticationFlowsException;


/**
 * manages the data like user's account lock, or number of login-retries, etc.
 * @author OhadR
 *
 */
public interface AuthenticationFlowsProcessor
{
	/**
	 * @param email
	 * @param password
	 * @param retypedPassword
	 * @param path: the server-path. used for building the link in the email
	 * @throws AuthenticationFlowsException
	 */
	public void createAccount(
			String email,
			String password,
			String retypedPassword,
			String path) throws AuthenticationFlowsException;


	/**
	 * 
	 * @param email
	 * @param serverPath - the server-path. used for building the link in the email
	 * @throws AuthenticationFlowsException
	 */
	public void handleForgotPassword( String email, String serverPath ) 
			throws AuthenticationFlowsException;
	
	/**
	 * 
	 * @param username
	 * @return boolean, passChangeRequired. true if change password is required.
	 */
	public boolean setLoginSuccessForUser(String username);


	public AuthenticationPolicy getAuthenticationSettings();

	public AccountState getAccountState(String email);


	public String getSecretAnswer(String email);

	public void setLoginFailureForUser(String email);

	public void sendUnlockAccountMail(String email, 
			String serverPath);

	public void setPassword(String email, String encodedPassword);
	
	Date getPasswordLastChangeDate(String email);


	/**
	 * 
	 * @param email
	 * @param encodedCurrentPassword
	 * @param encodedNewPassword
	 * @return pair of strings. left is the status OK | ERROR, right is the message
	 */
	public Pair<String, String> changePassword(String email, String encodedCurrentPassword,
			String newEncodedPassword);

	public void setEnabled(String userEmail);

	/**
	 * NOT IMPLEMENTED:
	 * @param email
	 * @return
	 * 
	public String getSecretQuestion(String email);
		 */

	public void validatePassword(String password,
			AuthenticationPolicy settings) throws AuthenticationFlowsException;
	
	public void validateRetypedPassword(String password, String retypedPassword) throws AuthenticationFlowsException;

	public String encodeString(String salt, String rawPass); 

}
