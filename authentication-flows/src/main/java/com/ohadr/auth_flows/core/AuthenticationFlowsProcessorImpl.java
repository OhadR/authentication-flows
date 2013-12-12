package com.ohadr.auth_flows.core;

import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.crypto.service.CryptoService;

@Component
public class AuthenticationFlowsProcessorImpl implements AuthenticationFlowsProcessor 
{
	private static Logger log = Logger.getLogger(AuthenticationFlowsProcessorImpl.class);
	
	@Autowired
	private AuthenticationAccountRepository repository;
	
	@Autowired
	private AuthenticationPolicyRepository policyRepo;
	
	@Autowired
	private CryptoService	cryptoService;
	
	@Autowired
	private MailSender		mailSender;

	@Autowired
	private AuthFlowsProperties properties;



	
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
			AuthenticationUser oauthUser = (AuthenticationUser) repository.loadUserByUsername( email );
			
			//if user exist, but not activated - we allow re-registration:
			if(oauthUser != null && !oauthUser.isEnabled())
			{
				repository.deleteUser( email );
			}

			AuthenticationUser user = new InMemoryAuthenticationUserImpl(
					email, encodedPassword, 
					false,									//start as de-activated
					properties.getMaxAttempts(),
					null);		//set by the repo-impl	

			repository.createUser(user);
					
/*			AccountState accountState = repository.createAccount(email, encodedPassword,
					properties.getMaxAttempts()
					//NOT IMPLEMENTED		secretQuestion, encodedAnswer
					);
			if(accountState == AccountState.ALREADY_EXIST)
			{
				log.error( "Account ALREADY_EXIST for user " + email );
				return Pair.of(FlowsConstatns.ERROR, "USER_ALREADY_EXIST");
			}
*/		}
		catch(DataIntegrityViolationException e)
		{
			//get the cause-exception, since it has a better message:
			Throwable root = e.getRootCause();
			String msg = root.getMessage();
			Assert.isTrue(msg.contains("Duplicate entry"));
			

			log.error( msg );
			return Pair.of(FlowsConstatns.ERROR, "USER_ALREADY_EXIST");
		}
		catch(AlreadyExistsException aee)
		{
			String msg = aee.getMessage();
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
		repository.setAttemptsLeft(username, properties.getMaxAttempts());
		
		return isPasswordChangeRequired(username);		
	}


	private boolean isPasswordChangeRequired(String username)
	{
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
		return policyRepo.getAuthenticationPolicy();
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
		AuthenticationUser user = (AuthenticationUser) repository.loadUserByUsername(email);
		
		if( 0 == user.getLoginAttemptsLeft() )
		{
			//lock the user:
			repository.setDisabled(email);
		}
		else
		{
			repository.decrementAttemptsLeft(email);
		}

	}

	@Override
	public void sendUnlockAccountMail(String email, 
			String serverPath)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String username, String newEncodedPassword) 
	{
		log.info("setting password for user " + username);
		repository.changePassword(username, newEncodedPassword);
	}
	

	@Override
	public Pair<String, String> changePassword(String username,
			String encodedCurrentPassword, String newEncodedPassword)
	{
		log.info("changing password for user " + username);
		
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
			log.error("passwords given by user and the one in the DB mismatch");
			
//			String isLocked = onLoginFailure(username);
//			boolean retVal = Boolean.valueOf(isLocked);

//			throw errorsHandler.createError(ApiErrors.CHANGE_PASSWORD_BAD_OLD_PASSWORD, username);
			return Pair.of(FlowsConstatns.ERROR, "CHANGE_PASSWORD_BAD_OLD_PASSWORD");
		}
		
		
		setPassword(username, newEncodedPassword);

		return Pair.of(FlowsConstatns.OK, "");
	}


	private void sendMail(String email, 
			String mailSubject,
			String mailBody)
	{
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(email);
		msg.setSubject(mailSubject);
		msg.setText(mailBody);
		mailSender.send(msg);
	}


	@Override
	public void setEnabled(String userEmail)
	{
		repository.setEnabled(userEmail);
	}
}
