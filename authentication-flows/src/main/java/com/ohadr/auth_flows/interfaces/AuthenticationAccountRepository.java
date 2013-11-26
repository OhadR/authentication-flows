package com.ohadr.auth_flows.interfaces;

import java.util.Date;

import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AuthenticationUser;


public interface AuthenticationAccountRepository 
{
	void createAccount(String email, 
			String encodedPassword
//			String secretQuestion, 		NOT IMPLEMENTED
//			String encodedAnswer		NOT IMPLEMENTED
			);

	/**
	 * 
	 * @param email
	 * @return null if username was not found
	 */
	AuthenticationUser getUser(String email);
	
	void deleteOAuthAccount(String email);

	void setEnabled(String email);

	boolean changePassword(String username, String newEncodedPassword);
	
	/**
	 * 
	 * @param email
	 * @param maxPasswordEntryAttempts TODO
	 * @return: true if account still open for next attempt. false if account is blocked (due to exceeded attempts number)
	 */
	boolean setLoginFailure(String email, int maxPasswordEntryAttempts); 
	void setLoginSuccess(String email);
	AccountState isAccountLocked(String email);

	/**
	 * sets a password for a given user
	 * @param email - the user's email
	 * @param newPassword - new password to set
	 * @return 
	 */
	boolean setPassword(String email, String newPassword); 
	
	String getEncodedPassword(String username);
	Date getPasswordLastChangeDate(String email);

	AuthenticationPolicy getAuthenticationPolicy();

	/**
	 * NOT IMPLEMENTED
	 * 
	 * 
	String getEncodedSecretAnswer(String email);
	*/


}
