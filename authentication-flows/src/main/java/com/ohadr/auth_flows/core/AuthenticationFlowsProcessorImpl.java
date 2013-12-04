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
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.interfaces.MailSender;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.crypto.service.CryptoService;

@Component
public class AuthenticationFlowsProcessorImpl implements AuthenticationFlowsProcessor 
{
	//TODO: read from "policy" table:
	private static final int maxPasswordEntryAttempts = 5;

	private static Logger log = Logger.getLogger(AuthenticationFlowsProcessorImpl.class);
	
	@Autowired
	private AuthenticationAccountRepository repository;
	
	@Autowired
	private CryptoService	cryptoService;
	
	@Autowired
	private MailSender		mailSender;


	
	@Override
	public Pair<String, String> createAccount(
			String email,
			String encodedPassword 
//			String secretQuestion,		NOT IMPLEMENTED
//			String encodedAnswer,		NOT IMPLEMENTED
//			String redirectUri			NOT IMPLEMENTED
			, 
			String serverPath
			)
	{
		log.info("Manager: createAccount() for user " + email);

		try
		{
			AuthenticationUser oauthUser = repository.getUser( email );
			
			//if user exist, but not activated - we allow re-registration:
			if(oauthUser != null && !oauthUser.isEnabled())
			{
				repository.deleteAccount( email );
			}

			AccountState accountState = repository.createAccount(email, encodedPassword
					//NOT IMPLEMENTED		secretQuestion, encodedAnswer
					);
			if(accountState == AccountState.ALREADY_EXIST)
			{
				log.error( "Account ALREADY_EXIST for user " + email );
				return Pair.of(FlowsConstatns.ERROR, "USER_ALREADY_EXIST");
			}
		}
		catch(DataIntegrityViolationException e)
		{
			//get the cause-exception, since it has a better message:
			Throwable root = e.getRootCause();
			String msg = root.getMessage();
			Assert.isTrue(msg.contains("Duplicate entry"));
			

			log.error( msg );
			return Pair.of(FlowsConstatns.ERROR, "USER_ALREADY_EXIST");
		}
		

		log.info("Manager: sending registration email to " + email + "...");

		
		String activationUrl = serverPath + FlowsConstatns.ACTIVATE_ACCOUNT_ENDPOINT +
			"?" + 
//			"a=" + FlowsConstatns.MailMessage.OAUTH_ACTIVATE_ACCOUNT + "&" + 
			"uts=" + cryptoService.createEncodedContent( new Date(System.currentTimeMillis()), email);
		
		sendMail(email,
				FlowsConstatns.MailMessage.AUTHENTICATION_MAIL_SUBJECT, 
				FlowsConstatns.MailMessage.AUTHENTICATION_MAIL_BODY + activationUrl);
		
		return ImmutablePair.of(FlowsConstatns.OK, "");
	}
	

	@Override
	public boolean setLoginSuccessForUser(String username) 
	{
		//via oAuthProcessor, since we want to UPDATE the DB:
		repository.resetAttemptsCounter(username);
		
		Date passwordLastChangeDate = repository.getPasswordLastChangeDate(username);
		
		//in case of 'demo' user (when the oauth client invokes actions like create account), the user will not be found in the DB:
		if(null == passwordLastChangeDate)
		{
			//error, technically:
			return false;
		}
		long passwordLastChange = passwordLastChangeDate.getTime();
		AuthenticationPolicy policy = getAuthenticationSettings();
		long passwordLifeInMilisecs = policy.getPasswordLifeInDays() * FlowsConstatns.DAY_IN_MILLI;
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
		return repository.getAuthenticationPolicy();
	}

	@Override
	public AccountState getAccountState(String email) 
	{
		return repository.isAccountLocked(email);
	}

	@Override
	public void sendPasswordRestoreMail(String email,
			String serverPath) 
	{
		String passwordRestoreUrl = serverPath + FlowsConstatns.RESTORE_PASSWORD_ENDPOINT +
				"?" + 
//				"a=" + FlowsConstatns.MailMessage.OAUTH_ACTIVATE_ACCOUNT + "&" + 
				"uts=" + cryptoService.createEncodedContent( new Date(System.currentTimeMillis()), email);

		sendMail(email,
				FlowsConstatns.MailMessage.RESTORE_PASSWORD_MAIL_SUBJECT, 
				FlowsConstatns.MailMessage.RESTORE_PASSWORD_MAIL_BODY + passwordRestoreUrl);

	}

	@Override
	public String getSecretAnswer(String email) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * the processor is a higher level than the repository. so when we increment, the proc should also check 
	 * if we crossed the max-attempts, and if so - lock the account. the repo simply does one function at a time.
	 */
	@Override
	public void setLoginFailureForUser(String email) 
	{
		AuthenticationUser user = repository.getUser(email);
		
		int attempts = user.getLoginAttemptsCounter();
		if(++attempts >= maxPasswordEntryAttempts)
		{
			//lock the user:
			repository.setDisabled(email);
		}
		else
		{
			repository.incrementAttemptsCounter(email);
		}

	}

	@Override
	public void sendUnlockAccountMail(String email, 
			String serverPath)
	{
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
		String dbPassword = repository.getEncodedPassword(username);
		if(dbPassword == null)
		{
//			throw errorsHandler.createError(ApiErrors.USER_NOT_EXIST, username);
			return Pair.of(FlowsConstatns.ERROR, "USER_NOT_EXIST");
		}
		if( !dbPassword.equals(encodedCurrentPassword) )
		{
			//password mismatch: error; update the counter and throw an error
//			String isLocked = onLoginFailure(username);
//			boolean retVal = Boolean.valueOf(isLocked);

//			throw errorsHandler.createError(ApiErrors.CHANGE_PASSWORD_BAD_OLD_PASSWORD, username);
			return Pair.of(FlowsConstatns.ERROR, "CHANGE_PASSWORD_BAD_OLD_PASSWORD");
		}
		
		
		if( changePassword(username, newEncodedPassword) )
		{
			return Pair.of(FlowsConstatns.OK, "");
		}
		else
		{
			return Pair.of(FlowsConstatns.ERROR, "CHANGE PASSWORD FAILED - DB");
		}
	}

	private boolean changePassword(String username, String newEncodedPassword) 
	{
		//via oAuthProcessor, since we want to UPDATE the DB:
		boolean changed = repository.changePassword(username, newEncodedPassword);
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
			String mailBody)
	{
		mailSender.sendMail(email, mailSubject, mailBody);
	}
}
