package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.service.CryptoService;
import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.FlowsConstatns;

@Controller
public class UserActionController
{

	private static final String LINK_HAS_EXPIRED = "link has expired";
	private static final String LINK_IS_INVALID = "link is invalid";
	private static final String ACCOUNT_CREATION_FAILED = "Account Creation Failed";
	public static final String PASSWORD_IS_INCORRECT = "password is incorrect";
	public static final String USER_DOES_NOT_EXIST = "user does not exist";
//	public static final String BAD_EMAIL_PARAM = "Bad email param";
	public static final String EMAIL_NOT_VALID = "The e-mail you have entered is not valid.";
	public static final String PASSWORD_CANNOT_BE_USED = "Your password is not acceptable by the organizational password policy.";
	public static final String PASSWORD_IS_TOO_LONG = "Password is too long";
	public static final String PASSWORD_IS_TOO_SHORT = "Password is too short";
	public static final String PASSWORD_TOO_FEW_LOWERS = "Password needs to contains at least %d lower-case characters";
	public static final String PASSWORD_TOO_FEW_UPPERS = "Password needs to contains at least %d upper-case characters";
	public static final String PASSWORD_TOO_FEW_NUMERICS = "Password needs to contains at least %d numeric characters";
	public static final String PASSWORD_TOO_FEW_SPECIAL_SYMBOLS = "Password needs to contains at least %d special symbols";
	private static final String ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH = 
			"Account creation has failed. These passwords don't match";
	public static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_NOT_COMPLIANT_WITH_POLICY = "Changing password has failed. Please note the password policy and try again.";
	public static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD = "CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD";
	public static final String ACCOUNT_CREATION_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE = 
			"Account creation has failed. Please note the password policy and try again. Error message: ";
	public static final String SECRET_ANSWER_CANNOT_CONTAIN_THE_PASSWORD_AND_VICE_VERSA = "Secret Answer cannot contain the password, and vice versa.";
	public static final String ACCOUNT_LOCKED_OR_DOES_NOT_EXIST = "Account is locked or does not exist";
	public static final String SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE =
			"Setting a new password has failed. Please note the password policy and try again. Error message: ";
	
	public static final String AN_EMAIL_WAS_SENT_TO_THE_GIVEN_ADDRESS_CLICK_ON_THE_LINK_THERE = "an email was sent to the given address. click on the link there";

	
	private static final String EMAIL_PARAM_NAME = "email";
	private static final String CONFIRM_PASSWORD_PARAM_NAME = "confirm_password";
//	private static final String LOGIN_ERROR_ATTRIB = "error";
	private static final String DELIMITER = "|";

	private static final String ERR_MSG = "err_msg";




	private static Logger log = Logger.getLogger(UserActionController.class);
	
	@Autowired
	private AuthFlowsProperties properties;

	@Autowired
	private CryptoService cryptoService;
	
//	@Autowired
//	private AbstractRememberMeServices rememberMeService;

	@Autowired
	private AuthenticationFlowsProcessor flowsProcessor;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * this let applications override this impl and add their custom functionality:
	 */
	@Autowired(required=false)
	private CreateAccountEndpoint createAccountEndpoint = new CreateAccountEndpoint();

	
	
	/**
	 * The UI calls this method in order to get the password policy
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/createAccountPage")
	final protected void createAccount( HttpServletResponse response) throws Exception
	{
		addPasswordConstraintsToResponse(response);
	}

	
	/**
	 * we get the params from the HTML-form. 
	 * this method is called by AJAX from "create-account" form submission
	 * @param name
	 * @param email
	 * @param password
	 * @param secretQuestion
	 * @param secretQuestionAnswer
	 * @param request
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	@RequestMapping("/createAccount")
	final protected View createAccount(
			@RequestParam(EMAIL_PARAM_NAME) String email,
			@RequestParam("password") String password,
			@RequestParam(CONFIRM_PASSWORD_PARAM_NAME) String retypedPassword,
//			@RequestParam("secretQuestion") String secretQuestion,						NOT IMPLEMENTED
//			@RequestParam("secretQuestionAnswer") String secretQuestionAnswer,			NOT IMPLEMENTED
//			@RequestParam(FlowsConstatns.REDIRECT_URI_PARAM_NAME) String redirectUri,	NOT IMPLEMENTED
			HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		PrintWriter writer = response.getWriter();

		RedirectView rv = new RedirectView();

//		request.setAttribute("email", email);
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(EMAIL_PARAM_NAME,  email);		

		
		//validate the input:
		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();
		
		String emailValidityMsg = validateEmail(email);
		if(!emailValidityMsg.equals(FlowsConstatns.OK))
		{
			log.error(emailValidityMsg + ": " + email);

			attributes.put(FlowsConstatns.ERR_MSG,  emailValidityMsg);		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "createAccount.jsp");
			return rv;
		}
		
		if( !password.equals(retypedPassword) )
		{
			log.error(ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);

			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "createAccount.jsp");
			return rv;
		}
		
		try
		{
			validatePassword(password, settings);
		}
		catch(AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );
			
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( afe.getMessage() ) );
//			return;


			attributes.put(FlowsConstatns.ERR_MSG,  afe.getMessage());		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "createAccount.jsp");
			return rv;
		}


		String encodedPassword = encodeString(email, password);


		String path = FlowsUtil.getServerPath(request);
		
		//make any other additional chackes. this let applications override this impl and add their custom functionality:
		try
		{
			createAccountEndpoint.additionalValidations( email, password );
		} 
		catch (AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );
			
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( afe.getMessage() ) );
//			return;


			attributes.put(FlowsConstatns.ERR_MSG,  afe.getMessage());		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "createAccount.jsp");
			return rv;
		}
		
		//TODO: let flowsProcessor.createAccount() throw exception if unsuccessful, instead of returning pair ...
		Pair<String, String> retVal = flowsProcessor.createAccount(email, encodedPassword, path);
    	if( ! retVal.getLeft().equals(FlowsConstatns.OK))
    	{
			String errorText = retVal.getRight();

			log.error(errorText);

			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_CREATION_FAILED + "; " + errorText);
			
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "createAccount.jsp");
			return rv;
    	}
        

        //update the "remember-me" token validity:
        int rememberMeTokenValidityInDays = settings.getRememberMeTokenValidityInDays();

        //get the "remem-me" bean and update its validity:
//        rememberMeService.setTokenValiditySeconds(rememberMeTokenValidityInDays * 60 * 60 * 24);
                

		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);
		rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "accountCreatedSuccess.jsp");
		return rv;

	}


	private String validateEmail(String email)
	{
		if( ! email.contains("@") )
		{
			return EMAIL_NOT_VALID;
		}
		return FlowsConstatns.OK;
	}



	private String encodeString(String salt, String rawPass) 
	{
		//encoding the password:
        String encodedPassword = passwordEncoder.encodePassword(rawPass, salt);	//the email is the salt
		return encodedPassword;
	}
	
	
	private void addPasswordConstraintsToResponse(HttpServletResponse response)
	{
		try
		{
			PrintWriter writer = response.getWriter();
			writer.println(FlowsConstatns.OK + DELIMITER);
			writer.write( buildPasswordConstraintsString() );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String buildPasswordConstraintsString()
	{
		StringBuilder builder = new StringBuilder();
//		builder.append("secretQuestions=");

		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();
		log.info("got the policy from API: " + settings.toString());


		String passwordConstraints = 
				"PasswordMaxLength="+ settings.getPasswordMaxLength()+ DELIMITER +
				"PasswordMinLength="+settings.getPasswordMinLength()+ DELIMITER +
				"PasswordMinLoCaseLetters="+settings.getPasswordMinLoCaseChars()+ DELIMITER +
				"PasswordMinNumbers="+settings.getPasswordMinNumbericDigits()+ DELIMITER +
				"PasswordMinSpecialSymbols="+settings.getPasswordMinSpecialSymbols()+ DELIMITER +
				"PasswordMinUpCaseLetters="+settings.getPasswordMinUpCaseChars()
				;


		builder.append( DELIMITER		//we sepoarate the secretQ's and the constraints with '|'. the last secretQ comes with a single '|' after it, so now add another one:
				+ passwordConstraints );

		return builder.toString();
	}


	/**********************************************************************************************************/
	/**
	 * (1)
	 * we got here after the user clicked "forgot password". user needs to enter his email before clicking, so when we get
	 * here, we already know his email address. if "secret question" is implemented, this is the place to look for 
	 * the 'question' in the DB (unless the impl forst send email and only then checks for the secret question)
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/forgotPasswordPage")
	protected View forgotPasswordPage(	
			@RequestParam(EMAIL_PARAM_NAME) String email,
			HttpServletRequest request) throws Exception
	{
		RedirectView rv = new RedirectView();

//		request.setAttribute("email", email);
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(EMAIL_PARAM_NAME,  email);		

		
		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = flowsProcessor.getAccountState(email);
		if( accountState != AccountState.OK )
		{
			//account has been locked/does not exist: notify user:
			log.error(ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);

			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);		
			attributes.put(FlowsConstatns.ERR_HEADER,  ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "error.jsp");
			return rv;
		}

		String path = FlowsUtil.getServerPath(request);

		flowsProcessor.sendPasswordRestoreMail(email, path);

		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);
		rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "passwordRestoreEmailSent.jsp");
		return rv;
	}

	/**********************************************************************************************************/

	/**
	 *  (3) 
	 *  called upon form submission
	 *  
	 * @param encUserAndTimestamp
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/setNewPassword")
	protected View setNewPassword( 
			@RequestParam(FlowsConstatns.HASH_PARAM_NAME) String encUserAndTimestamp,
			@RequestParam("password") String password,
			@RequestParam(CONFIRM_PASSWORD_PARAM_NAME) String retypedPassword)
	{
		RedirectView rv = new RedirectView();
		Map<String, String> attributes = new HashMap<String, String>();

		String email;
		try
		{
			validateRetypedPassword(password, retypedPassword);

			//validations: (using Fiddlr, hacker can hack this URL *AFTER* changing password to himself, and 
			//renaming the user to someone else.
			ImmutablePair<Date, String> stringAndDate = null;
			stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
			
			validateExpiration(stringAndDate.getLeft());

			email = stringAndDate.getRight();

			//after validations, make the work: validate password constraints, and update DB:

			//validate the input:
			AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();

			validatePassword(password, settings);
		}
		catch(AuthenticationFlowsException afe)
		{		
			log.error( afe.getMessage() );

			attributes.put(FlowsConstatns.ERR_MSG,  afe.getMessage());		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
			return rv;
		}
		catch(CryptoException cryptoEx)
		{
			log.error( LINK_IS_INVALID + "; exception message: " + cryptoEx.getMessage() );

			attributes.put(FlowsConstatns.ERR_MSG,  LINK_IS_INVALID + "; exception message: " + cryptoEx.getMessage() );		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
			return rv;
		}
		

		String encodedPassword = encodeString(email, password);

		//use API to go to the DB and update the password, and activate the account:
		flowsProcessor.setPassword(email, encodedPassword);

		attributes.put(EMAIL_PARAM_NAME,  email);		
		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);
		rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "passwordSetSuccess.jsp");
		return rv;
	}
	/**********************************************************************************************************/

	
	/**********************************************************************************************************/
	/**
	 * called by the UI, changePassword.jsp#submit
	 * 
	 * @param email
	 * @param currentPassword
	 * @param newPassword
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/changePassword")
	protected void changePassword( 
								@RequestParam("currentPassword") String currentPassword,
								@RequestParam("newPassword") String newPassword,
								@RequestParam(CONFIRM_PASSWORD_PARAM_NAME) String retypedPassword,
								@RequestParam(FlowsConstatns.ENCRYPTED_USERNAME_PARAM_NAME) String encUser,
								HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();
		

		if( !newPassword.equals(retypedPassword) )
		{
			log.error(ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);

//			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);		
			//adding attributes to the redirect return value:
//			rv.setAttributesMap(attributes);
//			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
//			return rv;
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);
			return;
		}

		String email = cryptoService.extractString(encUser);
		

		// we need to check is account locked?! (for hackers...)
		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = flowsProcessor.getAccountState(email);
		if( accountState != AccountState.OK )
		{
			//account has been locked: do not check the user's answer, but notify user:
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);
			return;
		}

		//validate the input:
		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();

		try
		{
			validatePassword(newPassword, settings);
		}
		catch(AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );

			//UI will redirect back to createAccount page, with error message:
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_NOT_COMPLIANT_WITH_POLICY + 
					" Error message: " + afe.getMessage()) );
			return;
		}
		
		
		if(currentPassword.equals(newPassword))
		{
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD ));
			return;
			
		}
		
		String encodedCurrentPassword = encodeString(email, currentPassword);
		String encodedNewPassword = encodeString(email, newPassword);

		//use API to go to the DB, validate current pswd and update the new one, and activate the account:
		Pair<String, String> retVal = flowsProcessor.changePassword(email, encodedCurrentPassword, encodedNewPassword);
		if( ! retVal.getLeft().equals(FlowsConstatns.OK))
		{
			String errorText = retVal.getRight();

			log.error(errorText);
			
			//error - old password is incorrect; redirect back to same page (with the email as param):
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + unescapeJaveAndEscapeHtml( PASSWORD_IS_INCORRECT ));

			return;
			
		}
		

		writer.println(FlowsConstatns.OK);

	}
	/**********************************************************************************************************/

	
	protected void isAccountLocked(@RequestParam(EMAIL_PARAM_NAME) String email, 
			HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();
		
		String isLockedStr = "false";
		
		AccountState accountState = flowsProcessor.getAccountState(email);
		if( accountState == AccountState.LOCKED )
		{
			isLockedStr = "true";
		}

		//account has been locked: send email and redirect to notify user:
		writer.println(FlowsConstatns.OK + DELIMITER + isLockedStr);
	}

	private static String unescapeJaveAndEscapeHtml(String input)
	{
		String tmp = StringEscapeUtils.unescapeJava( input );
		return StringEscapeUtils.escapeHtml( tmp );
	}

	private void validateRetypedPassword(String password, String retypedPassword) throws AuthenticationFlowsException
	{
		if(!password.equals(retypedPassword))
		{
			throw new AuthenticationFlowsException(ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);
		}
	}
	
	private void validateExpiration(Date linkCreationDate) throws AuthenticationFlowsException
	{
		boolean expired = (System.currentTimeMillis() - linkCreationDate.getTime()) > (properties.getLinksExpirationMinutes() * 1000 * 60L);
		if( expired )
		{
			throw new AuthenticationFlowsException(LINK_HAS_EXPIRED);
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
}
