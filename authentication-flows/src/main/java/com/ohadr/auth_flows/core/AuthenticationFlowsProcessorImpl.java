package com.ohadr.auth_flows.core;

import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.interfaces.MailSender;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AuthenticationUser;
import com.ohadr.authentication.utils.oAuthConstants;

@Component
public class AuthenticationFlowsProcessorImpl implements AuthenticationFlowsProcessor 
{
	private static Logger log = Logger.getLogger(AuthenticationFlowsProcessorImpl.class);
	
	@Autowired
	private AuthenticationAccountRepository oAuthRepository;
	
	@Autowired
	private MailSender		mailSender;


	
	@Override
	public Pair<String, String> createAccount(
			String email,
			String encodedPassword 
//			String secretQuestion,		NOT IMPLEMENTED
//			String encodedAnswer,		NOT IMPLEMENTED
//			String redirectUri			NOT IMPLEMENTED
			)
	{
		log.info("Manager: createAccount() for user " + email);

		try
		{
			AuthenticationUser oauthUser = oAuthRepository.getUser( email );
			
			//if user exist, but not activated - we allow re-registration:
			if(oauthUser != null && !oauthUser.getEnabled())
			{
				oAuthRepository.deleteOAuthAccount( email );
			}

			oAuthRepository.createAccount(email, encodedPassword
					//NOT IMPLEMENTED		secretQuestion, encodedAnswer
					);
		}
		catch(DataIntegrityViolationException e)
		{
			//get the cause-exception, since it has a better message:
			Throwable root = e.getRootCause();
			String msg = root.getMessage();
			Assert.isTrue(msg.contains("Duplicate entry"));
			

			log.error( msg );
			return Pair.of(oAuthConstants.ERROR, "USER_ALREADY_EXIST");
		}
		

		log.info("Manager: sending registration email to " + email + "...");

		sendMail(email,
				oAuthConstants.MailMessage.AUTHENTICATION_MAIL_SUBJECT, 
				oAuthConstants.MailMessage.OAUTH_AUTHENTICATION_MAIL_BODY,
				oAuthConstants.Authentication.OAUTH_ACTIVATE_ACCOUNT );
		
		return ImmutablePair.of(oAuthConstants.OK, "");
	}
	

	@Override
	public boolean setLoginSuccessForUser(String username) 
	{
		//via oAuthProcessor, since we want to UPDATE the DB:
		oAuthRepository.setLoginSuccess(username);
		
		Date passwordLastChangeDate = oAuthRepository.getPasswordLastChangeDate(username);
		
		//in case of 'demo' user (when the oauth client invokes actions like create account), the user will not be found in the DB:
		if(null == passwordLastChangeDate)
		{
			//error, technically:
			return false;
		}
		long passwordLastChange = passwordLastChangeDate.getTime();
		AuthenticationPolicy policy = getAuthenticationSettings();
		long passwordLifeInMilisecs = policy.getPasswordLifeInDays() * oAuthConstants.DAY_IN_MILLI;
		Date passwordLimitDate = new Date( passwordLastChange + passwordLifeInMilisecs );
		Date current = new Date(System.currentTimeMillis());
		
		//if current is after the pass-limit, then user must change his password.
		boolean passChangeRequired =  current.after( passwordLimitDate );
		if(passChangeRequired)
		{
			log.info("password expired for user " + username);
		}

		return passChangeRequired;		
	}



	@Override
	public AuthenticationPolicy getAuthenticationSettings() 
	{
		return oAuthRepository.getAuthenticationPolicy();
	}

	@Override
	public AccountState isAccountLocked(String email) 
	{
		return oAuthRepository.isAccountLocked(email);
	}

	@Override
	public void sendPasswordRestoreMail(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSecretAnswer(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setLoginFailureForUser(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendUnlockAccountMail(String email, String redirectUri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String email, String encodedPassword) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pair<String, String> changePassword(String username,
			String encodedCurrentPassword, String newEncodedPassword)
	{
		//validate current password:
		String dbPassword = oAuthRepository.getEncodedPassword(username);
		if(dbPassword == null)
		{
//			throw errorsHandler.createError(ApiErrors.USER_NOT_EXIST, username);
			return Pair.of(oAuthConstants.ERROR, "USER_NOT_EXIST");
		}
		if( !dbPassword.equals(encodedCurrentPassword) )
		{
			//password mismatch: error; update the counter and throw an error
//			String isLocked = onLoginFailure(username);
//			boolean retVal = Boolean.valueOf(isLocked);

//			throw errorsHandler.createError(ApiErrors.CHANGE_PASSWORD_BAD_OLD_PASSWORD, username);
			return Pair.of(oAuthConstants.ERROR, "CHANGE_PASSWORD_BAD_OLD_PASSWORD");
		}
		
		
		if( changePassword(username, newEncodedPassword) )
		{
			return Pair.of(oAuthConstants.OK, "");
		}
		else
		{
			return Pair.of(oAuthConstants.ERROR, "CHANGE PASSWORD FAILED - DB");
		}
	}

	private boolean changePassword(String username, String newEncodedPassword) 
	{
		//via oAuthProcessor, since we want to UPDATE the DB:
		boolean changed = oAuthRepository.changePassword(username, newEncodedPassword);
		if(changed)
		{
			log.info("changing password for user " + username);
		}
		else
		{
			log.error("could not change password to user " + username);
		}
		return changed;
	}



	private void sendMail(String email, 
			String mailSubject,
			String mailBody, 
			String action)
	{
		mailSender.sendMail(email, mailSubject, mailBody);
	}
}
