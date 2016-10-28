package com.ohadr.auth_flows.core;

import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.Assert;

import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.endpoints.CreateAccountEndpoint;
import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.mocks.InMemoryAuthenticationUserImpl;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationFlowsException;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.crypto.service.CryptoService;

@Component
public class AuthenticationFlowsProcessorImpl implements AuthenticationFlowsProcessor 
{
	private static Logger log = Logger.getLogger(AuthenticationFlowsProcessorImpl.class);
	
	public static final String EMAIL_NOT_VALID = "The e-mail you have entered is not valid.";
	public static final String USER_ALREADY_EXIST = "USER_ALREADY_EXIST";

	private static final String PASSWORD_CANNOT_BE_USED = "Your password is not acceptable by the organizational password policy.";
	private static final String PASSWORD_IS_TOO_LONG = "Password is too long";
	private static final String PASSWORD_IS_TOO_SHORT = "Password is too short";
	private static final String PASSWORD_TOO_FEW_LOWERS = "Password needs to contains at least %d lower-case characters";
	private static final String PASSWORD_TOO_FEW_UPPERS = "Password needs to contains at least %d upper-case characters";
	private static final String PASSWORD_TOO_FEW_NUMERICS = "Password needs to contains at least %d numeric characters";
	private static final String PASSWORD_TOO_FEW_SPECIAL_SYMBOLS = "Password needs to contains at least %d special symbols";
	private static final String SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE =
			"Setting a new password has failed. Please note the password policy and try again. Error message: ";
	public static final String ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH = 
			"Account creation has failed. These passwords don't match";
	
	private static final String ACCOUNT_LOCKED_OR_DOES_NOT_EXIST = "Account is locked or does not exist";

	private static final String LINK_HAS_EXPIRED = "link has expired";

	private static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD = "CHANGE PASSWORD FAILED: New Password is same as Old Password.";
	private static final String CHANGE_PASSWORD_BAD_OLD_PASSWORD = "CHANGE PASSWORD Failed: Bad Old Password.";


	@Autowired
	private AuthenticationAccountRepository repository;
	
	@Autowired
	private AuthenticationPolicyRepository policyRepo;
	
	@Autowired
	private CryptoService	cryptoService;
	
	@Autowired
	private MailSender			mailSender;
	
	@Autowired
    private VelocityEngine 		velocityEngine;


	@Autowired
	private AuthFlowsProperties properties;

	/**
	 * this let applications override this impl and add their custom functionality:
	 */
	@Autowired(required=false)
	private CreateAccountEndpoint createAccountEndpoint = new CreateAccountEndpoint();

	@Autowired
	private PasswordEncoder passwordEncoder;
	




	@Override
	public void createAccount(
			String email,
			String password,
			String retypedPassword,
            String firstName, 
			String lastName, 
			String path) throws AuthenticationFlowsException
	{
		//validate the input:
		AuthenticationPolicy settings = getAuthenticationSettings();
		
		validateEmail(email);
		
		validateRetypedPassword(password, retypedPassword);

		validatePassword(password, settings);


		String encodedPassword = encodeString(email, password);


		//make any other additional chackes. this let applications override this impl and add their custom functionality:
		createAccountEndpoint.additionalValidations( email, password );
		
		internalCreateAccount(email, encodedPassword, firstName, lastName, path);
        

        //update the "remember-me" token validity:
        int rememberMeTokenValidityInDays = settings.getRememberMeTokenValidityInDays();

        //get the "remem-me" bean and update its validity:
//        rememberMeService.setTokenValiditySeconds(rememberMeTokenValidityInDays * 60 * 60 * 24);
	}

	
	
	/**
	 * 
	 * @param email
	 * @param encodedPassword
	 * @param firstName
	 * @param lastName
	 * @param serverPath
	 * @throws AuthenticationFlowsException
	 */
	private void internalCreateAccount(
			String email,
			String encodedPassword, 
			String firstName, 
			String lastName, 
			String serverPath
			) throws AuthenticationFlowsException
	{
		String baseUrlPath=properties.getBaseUrlPath();
		String finalPath;
		email = email.toLowerCase();		// issue #23 : username is case-sensitive (https://github.com/OhadR/oAuth2-sample/issues/23)
		log.info("createAccount() for user " + email);

		try
		{
			AuthenticationUser oauthUser = null;
			try
			{
				oauthUser = (AuthenticationUser) repository.loadUserByUsername( email );
			}
			catch(UsernameNotFoundException unfe)
			{
				//basically do nothing - we expect user not to be found.
			}
			
			//if user exist, but not activated - we allow re-registration:
			if(oauthUser != null)
			{
				if( !oauthUser.isEnabled())
				{
					repository.deleteUser( email );
				}
				else
				{
					//error - user already exists and active
					log.error( "cannot create account - user " + email + " already exist." );
					throw new AuthenticationFlowsException( USER_ALREADY_EXIST );
				}
			}

			Collection<? extends GrantedAuthority> authorities = setAuthorities();		//set authorities
			AuthenticationUser user = new InMemoryAuthenticationUserImpl(
					email, encodedPassword, 
					false,									//start as de-activated
					policyRepo.getDefaultAuthenticationPolicy().getMaxPasswordEntryAttempts(),
					null,					//set by the repo-impl
					firstName,
					lastName,
					authorities);			

			repository.createUser(user);
			
			createAccountEndpoint.postCreateAccount( email );
		}
		//we should not get to these exceptions since we check earlier if account already exist (so repo's do not 
		// have to check it)
		catch(DataIntegrityViolationException e)
		{
			//get the cause-exception, since it has a better message:
			Throwable root = e.getRootCause();
			String msg = root.getMessage();
			Assert.isTrue(msg.contains("Duplicate entry"));
			

			log.error( msg );
			throw new AuthenticationFlowsException( USER_ALREADY_EXIST );
		}
		

		log.info("Manager: sending registration email to " + email + "...");

		if((baseUrlPath!=null) && (!baseUrlPath.isEmpty()))
			finalPath=baseUrlPath;
		else
			finalPath=serverPath;

		String activationUrl = finalPath + FlowsConstatns.ACTIVATE_ACCOUNT_ENDPOINT +
			"?" + 
//			"a=" + FlowsConstatns.MailMessage.OAUTH_ACTIVATE_ACCOUNT + "&" + 
			"uts=" + cryptoService.createEncodedContent( new Date(System.currentTimeMillis()), email);
		
		        
		try
		{
			sendMail(email,
					FlowsConstatns.MailMessage.AUTHENTICATION_MAIL_SUBJECT,
					"authentication.vm",
					activationUrl );
		}
		catch (MailException me)
		{
			log.error( me.getMessage() );
			throw new AuthenticationFlowsException( me.getMessage() );
		}
	}
	
	
	@Override
	public void handleForgotPassword( String email, String serverPath ) 
			throws AuthenticationFlowsException
	{
		validateEmail(email);

		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = getAccountState(email);
		if( accountState != AccountState.OK )
		{
			throw new AuthenticationFlowsException( ACCOUNT_LOCKED_OR_DOES_NOT_EXIST );
		}

		sendPasswordRestoreMail(email, serverPath);
	}
	
	
	
	@Override
	public String handleSetNewPassword( 
			String encUserAndTimestamp,
			String password,
			String retypedPassword) throws AuthenticationFlowsException
	{
		validateRetypedPassword(password, retypedPassword);

		//validations: (using Fiddlr, hacker can hack this URL *AFTER* changing password to himself, and 
		//renaming the user to someone else.
		ImmutablePair<Date, String> stringAndDate =
				cryptoService.extractStringAndDate( encUserAndTimestamp );
		
		validateExpiration(stringAndDate.getLeft());

		String email = stringAndDate.getRight();

		//after validations, make the work: validate password constraints, and update DB:

		//validate the input:
		AuthenticationPolicy settings = getAuthenticationSettings();

		validatePassword(password, settings);

		

		String encodedPassword = encodeString(email, password);

		// go to the DB and: (1) update the password, and (2) activate the account:
		setPassword(email, encodedPassword);
		
		return email;
	}

	
	
	@Override
	public String handleChangePassword( 
			String currentPassword,
			String newPassword,
			String retypedPassword,
			String encUser) throws AuthenticationFlowsException
	{
		String email = cryptoService.extractString(encUser);
		
		internalHandleChangePassword(currentPassword, newPassword, retypedPassword, email);
		return email;
	}

	@Override
	public String handleChangePassword( 
			String currentPassword,
			String newPassword,
			String retypedPassword) throws AuthenticationFlowsException
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName(); //get logged in username
		internalHandleChangePassword(currentPassword, newPassword, retypedPassword, email);
		return email;
	}

	public void internalHandleChangePassword( 
			String currentPassword,
			String newPassword,
			String retypedPassword,
			String email) throws AuthenticationFlowsException
	{
		validateRetypedPassword(newPassword, retypedPassword);
		
		// we need to check is account locked?! (for hackers...)
		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = getAccountState(email);
		if( accountState != AccountState.OK )
		{
			throw new AuthenticationFlowsException( ACCOUNT_LOCKED_OR_DOES_NOT_EXIST );
		}
		
		//validate the input:
		AuthenticationPolicy settings = getAuthenticationSettings();
		
		validatePassword(newPassword, settings);
		
		
		if( currentPassword.equals(newPassword) )
		{
			throw new AuthenticationFlowsException( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD );
		}
		
		String encodedCurrentPassword = encodeString(email, currentPassword);
		String encodedNewPassword = encodeString(email, newPassword);
		
		//go to the DB, validate current pswd and update the new one:
		invokeChangePassword(email, encodedCurrentPassword, encodedNewPassword);
	}

	
	
	

	private Collection<? extends GrantedAuthority> setAuthorities() 
	{
		Set<GrantedAuthority> set = new HashSet<GrantedAuthority>();
		GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_USER");
		set.add(auth);
		return set;		
	}

	@Override
	public boolean setLoginSuccessForUser(String username) 
	{
		log.debug("setting login success for user " + username);
		repository.setAttemptsLeft( username,
				getAuthenticationSettings().getMaxPasswordEntryAttempts() );
		
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
		int passwordLifeInDays = policy.getPasswordLifeInDays();
		if( passwordLifeInDays == FlowsConstatns.ETERNAL_PASSWORD )
		{
			return false;
		}
		
		long passwordLifeInMilisecs = passwordLifeInDays * FlowsConstatns.DAY_IN_MILLI;
		Date passwordLimitDate = new Date( passwordLastChange + passwordLifeInMilisecs );
		Date current = new Date(System.currentTimeMillis());
		
		//if current is after the pass-limit, then user must change his password.
		boolean passChangeRequired =  current.after( passwordLimitDate );
		if(passChangeRequired)
		{
			log.info("password expired for user " + username);
//			log.info("getPasswordLifeInDays(): " + policy.getPasswordLifeInDays() + 
//					" passwordLastChangeDate: " + passwordLastChangeDate + 
//					" passwordLimitDate: " + passwordLimitDate + 
//					" passwordLifeInMilisecs: " + passwordLifeInMilisecs);
		}

		return passChangeRequired;
	}



	@Override
	public AuthenticationPolicy getAuthenticationSettings() 
	{
		return policyRepo.getDefaultAuthenticationPolicy();
	}

	@Override
	public AccountState getAccountState(String email) 
	{
		return repository.isAccountLocked(email);
	}

	private void sendPasswordRestoreMail(String email,
			String serverPath) 
	{
		String passwordRestoreUrl;
		String finalPath;
		String baseUrlPath=properties.getBaseUrlPath();

		if((baseUrlPath!=null) && (!baseUrlPath.isEmpty()))
                        finalPath=baseUrlPath;
                else
                        finalPath=serverPath;

		passwordRestoreUrl = finalPath + FlowsConstatns.RESTORE_PASSWORD_ENDPOINT +
				"?" + 
//				"a=" + FlowsConstatns.MailMessage.OAUTH_ACTIVATE_ACCOUNT + "&" + 
				"uts=" + cryptoService.createEncodedContent( new Date(System.currentTimeMillis()), email);

		sendMail(email,
				FlowsConstatns.MailMessage.RESTORE_PASSWORD_MAIL_SUBJECT,
				"restorePassword.vm",
				passwordRestoreUrl );
	}

	/**
	 * the processor is a higher level than the repository. so when we increment, the proc should also check 
	 * if we crossed the max-attempts, and if so - lock the account. the repo simply does one function at a time.
	 */
	@Override
	public void setLoginFailureForUser(String email) 
	{
		AuthenticationUser user = null;
		try
		{
			user = (AuthenticationUser) repository.loadUserByUsername(email);
		}
		catch(UsernameNotFoundException unfe)
		{
			return;
		}
		
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
                String finalPath;
                String baseUrlPath=properties.getBaseUrlPath();

                if((baseUrlPath!=null) && (!baseUrlPath.isEmpty()))
                        finalPath=baseUrlPath;
                else
                        finalPath=serverPath;
		
		log.info("Manager: sending Unlock-Account email to " + email + "...");
		
		String activationUrl = finalPath + FlowsConstatns.ACTIVATE_ACCOUNT_ENDPOINT +
			"?" + 
			"uts=" + cryptoService.createEncodedContent( new Date(System.currentTimeMillis()), email);
		
		sendMail(email,
				FlowsConstatns.MailMessage.UNLOCK_MAIL_SUBJECT,
				"accountLocked.vm",
				activationUrl );
		
	}

	@Override
	public void setPassword(String username, String newEncodedPassword) 
	{
		log.info("setting password for user " + username);
		repository.changePassword(username, newEncodedPassword);
	}
	

	private void invokeChangePassword(String username,
			String encodedCurrentPassword, String newEncodedPassword) throws AuthenticationFlowsException
	{
		log.info("changing password for user " + username);
		
		//validate current password:
		String dbPassword = repository.getEncodedPassword(username);
		if(dbPassword == null)
		{
			throw new AuthenticationFlowsException( "USER_NOT_EXIST" );
		}
		if( !dbPassword.equals(encodedCurrentPassword) )
		{
			//password mismatch: error; update the counter and throw an error
			log.error("passwords given by user and the one in the DB mismatch");
			
//			String isLocked = onLoginFailure(username);
//			boolean retVal = Boolean.valueOf(isLocked);

			throw new AuthenticationFlowsException( CHANGE_PASSWORD_BAD_OLD_PASSWORD );
		}
		
		setPassword(username, newEncodedPassword);
	}


	private void sendMail(String email, 
			String mailSubject,
			String msgTextFileName,
			String urlInMessage)
	{
		Map<String, Object> model = new HashMap<String, Object>();
        model.put("username", email);
        model.put("url", urlInMessage);
        String mailBody = VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, getResourcePath( msgTextFileName ), model);

        SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(email);
		msg.setSubject(mailSubject);
		msg.setText(mailBody);
		
		mailSender.send( msg );
	}


	@Override
	public void setEnabled(String userEmail)
	{
		repository.setEnabled(userEmail);
	}


	@Override
	public Date getPasswordLastChangeDate(String email)
	{
		return repository.getPasswordLastChangeDate(email);
	}
	
	private static String getResourcePath(String name)
	{
		return "mailTemplates/" + Locale.getDefault().getLanguage() + "/" + name;
	}
	
	
	private void validateEmail(String email) throws AuthenticationFlowsException
	{
		if( ! email.contains("@") )
		{
			throw new AuthenticationFlowsException( EMAIL_NOT_VALID );
		}
	}

	private void validateRetypedPassword(String password, String retypedPassword) throws AuthenticationFlowsException
	{
		if(!password.equals(retypedPassword))
		{
			throw new AuthenticationFlowsException(ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);
		}
	}

	private void validatePassword(String password,
			AuthenticationPolicy settings) throws AuthenticationFlowsException 
	{
		List<String> blackList = settings.getPasswordBlackList();
		if(blackList != null)
		{
			for(String forbidenPswd : blackList)
			{
				if(password.equalsIgnoreCase(forbidenPswd))
				{
					throw new AuthenticationFlowsException(SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + "; " + PASSWORD_CANNOT_BE_USED);
				}
			}
		}

		
		if(password.length() > settings.getPasswordMaxLength())
		{
			throw new AuthenticationFlowsException(SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + "; " + PASSWORD_IS_TOO_LONG);
		}

		if(password.length() < settings.getPasswordMinLength())
		{
			throw new AuthenticationFlowsException(SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + "; " + PASSWORD_IS_TOO_SHORT);
		}
		
		int uppersCounter = 0;
		int lowersCounter = 0;
		int numericCounter = 0;
		int specialSymbolCounter = 0;
		char[] dst = new char[password.length()];
		password.getChars(0, password.length(), dst, 0);
		for(int i=0; i<password.length(); ++i)
		{
			if(Character.isUpperCase(dst[i]))
			{
				++uppersCounter;
			}
			else if(Character.isLowerCase(dst[i]))
			{
				++lowersCounter;
			}
			else if(Character.isDigit(dst[i]))
			{
				++numericCounter;
			}
			else
			{
				//not digit and not a letter - consider it as a 'special symbol':
				++specialSymbolCounter;
			}
		}
		
		Formatter formatter = new Formatter();

		String retVal = "";
		
		if(uppersCounter < settings.getPasswordMinUpCaseChars())
		{
			retVal = formatter.format(PASSWORD_TOO_FEW_UPPERS, settings.getPasswordMinUpCaseChars()).toString() ;
		}
		if(lowersCounter < settings.getPasswordMinLoCaseChars())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_LOWERS, settings.getPasswordMinLoCaseChars()).toString();
		}
		if(numericCounter < settings.getPasswordMinNumbericDigits())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_NUMERICS, settings.getPasswordMinNumbericDigits()).toString();
		}
		if(specialSymbolCounter < settings.getPasswordMinSpecialSymbols())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_SPECIAL_SYMBOLS, settings.getPasswordMinSpecialSymbols()).toString();
		}
		
		formatter.close();
		
		if(!retVal.isEmpty())
		{
			throw new AuthenticationFlowsException(SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + "; " + retVal);
		}
	}

	private String encodeString(String salt, String rawPass) 
	{
		//encoding the password:
        String encodedPassword = passwordEncoder.encodePassword(rawPass, salt);	//the email is the salt
		return encodedPassword;
	}

	private void validateExpiration(Date linkCreationDate) throws AuthenticationFlowsException
	{
		boolean expired = (System.currentTimeMillis() - linkCreationDate.getTime()) > (properties.getLinksExpirationMinutes() * 1000 * 60L);
		if( expired )
		{
			throw new AuthenticationFlowsException( LINK_HAS_EXPIRED );
		}
	}
	
}
