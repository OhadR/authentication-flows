package com.ohadr.auth_flows.interfaces;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.UserDetailsManager;

import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


public interface AuthenticationAccountRepository extends UserDetailsManager
{
    AccountState createAccount(String email, 
			String encodedPassword,
//			String secretQuestion, 		NOT IMPLEMENTED
//			String encodedAnswer		NOT IMPLEMENTED
			int numLoginAttemptsAllowed);

	void deleteAccount(String email);

	void setEnabled(String email);
	void setDisabled(String email);
	boolean isActivated(String email);

	AccountState isAccountLocked(String email);

//	boolean changePassword(String username, String newEncodedPassword);
	
	/**
	 * 
	 * @param email
	 */
	void decrementAttemptsLeft(String email); 
	void setAttemptsLeft(String email, int numAttemptsAllowed);

	/**
	 * sets a password for a given user
	 * @param email - the user's email
	 * @param newPassword - new password to set
	 */
	void setPassword(String email, String newPassword); 
	
	String getEncodedPassword(String username);
	Date getPasswordLastChangeDate(String email);

	AuthenticationPolicy getAuthenticationPolicy();

	/**
	 * NOT IMPLEMENTED
	 * 
	 * 
	String getEncodedSecretAnswer(String email);
	*/

	/**
	 * this method is not needed anymore since we extend 
	 * {@link org.springframework.security.core.userdetails.UserDetailsService} 
	 * with its <code>loadUserByUsername(String username)</code>
	 *  
	 * @param email
	 * @return null if username was not found
	 * /
	AuthenticationUser getUser(String email);
	*/

}
