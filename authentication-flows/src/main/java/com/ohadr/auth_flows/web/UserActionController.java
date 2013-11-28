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
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;

import com.ohadr.authentication.utils.oAuthConstants;
import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.service.CryptoService;
import com.ohadr.auth_flows.config.OAuthServerProperties;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AccountState;

@Controller
public class UserActionController
{

	public static final String PASSWORD_IS_INCORRECT = "password is incorrect";
	public static final String USER_DOES_NOT_EXIST = "user does not exist";
	public static final String BAD_EMAIL_PARAM = "Bad email param";
	public static final String PASSWORD_CANNOT_BE_USED = "Your password is not acceptable by the organizational password policy.";
	public static final String PASSWORD_IS_TOO_LONG = "Password is too long";
	public static final String PASSWORD_IS_TOO_SHORT = "Password is too short";
	public static final String PASSWORD_TOO_FEW_LOWERS = "Password needs to contains at least %d lower-case characters";
	public static final String PASSWORD_TOO_FEW_UPPERS = "Password needs to contains at least %d upper-case characters";
	public static final String PASSWORD_TOO_FEW_NUMERICS = "Password needs to contains at least %d numeric characters";
	public static final String PASSWORD_TOO_FEW_SPECIAL_SYMBOLS = "Password needs to contains at least %d special symbols";
	public static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_NOT_COMPLIANT_WITH_POLICY = "Changing password has failed. Please note the password policy and try again.";
	public static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD = "CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD";
	public static final String ACCOUNT_CREATION_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE = "Account creation has failed. Please note the password policy and try again. Error message: ";
	public static final String SECRET_ANSWER_CANNOT_CONTAIN_THE_PASSWORD_AND_VICE_VERSA = "Secret Answer cannot contain the password, and vice versa.";
	public static final String ACCOUNT_CREATION_HAS_FAILED_PLEASE_TRY_AGAIN_ERROR_MESSAGE = "Account creation has failed. Please try again. Error message: ";
	public static final String ACCOUNT_LOCKED_OR_DOES_NOT_EXIST = "account locked or does not exist";
	public static final String SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE = "Setting a new password has failed. Please note the password policy and try again. Error message: ";
	public static final String AN_EMAIL_WAS_SENT_TO_THE_GIVEN_ADDRESS_CLICK_ON_THE_LINK_THERE = "an email was sent to the given address. click on the link there";

	
	private static final String EMAIL_PARAM_NAME = "email";
//	private static final String LOGIN_ERROR_ATTRIB = "error";
	private static final String ERR_MSG = "err_msg";
	private static final String DELIMITER = "|";



	private static Logger log = Logger.getLogger(UserActionController.class);
	
	@Autowired
	private OAuthServerProperties oAuthServerProperties;

	@Autowired
	private CryptoService cryptoService;
	
	@Autowired
	private AbstractRememberMeServices rememberMeService;

	@Autowired
	private AuthenticationFlowsProcessor flowsProcessor;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	
	
	/**
	 * The UI calls this method in order to get the password policy
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/createAccountPage")
	protected void createAccount( HttpServletResponse response) throws Exception
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
	 * @throws Exception
	 */
	@RequestMapping("/createAccount")
	protected View createAccount(
			@RequestParam(EMAIL_PARAM_NAME) String email,
			@RequestParam("password") String password,
//			@RequestParam("secretQuestion") String secretQuestion,						NOT IMPLEMENTED
//			@RequestParam("secretQuestionAnswer") String secretQuestionAnswer,			NOT IMPLEMENTED
//			@RequestParam(oAuthConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,	NOT IMPLEMENTED
			HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		RedirectView rv = new RedirectView();

//		InternalResourceView irv = new InternalResourceView();

		PrintWriter writer = response.getWriter();

		//validate the input:
		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();
		
		String passwordValidityMsg = validatePassword(password, settings);
		if( !passwordValidityMsg.equals(oAuthConstants.OK) )
		{
			//redirect back to createAccount page, with error message:
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( ACCOUNT_CREATION_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + passwordValidityMsg ) );
			return rv;
		}


		String encodedPassword = encodeString(email, password);

    	Pair<String, String> retVal = flowsProcessor.createAccount(email, encodedPassword);
    	if( ! retVal.getLeft().equals(oAuthConstants.OK))
    	{
			String errorText = retVal.getRight();

			log.error(errorText);

			//redirecting back to the same page, just add a message to the screen and let the user re-try:
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( ACCOUNT_CREATION_HAS_FAILED_PLEASE_TRY_AGAIN_ERROR_MESSAGE + errorText ));
			return rv;
    	}
        

        //update the "remember-me" token validity:
        int rememberMeTokenValidityInDays = settings.getRememberMeTokenValidityInDays();
        //get the "remem-me" bean and update its validity:
		rememberMeService.setTokenValiditySeconds(rememberMeTokenValidityInDays * 60 * 60 * 24);
                

//		request.setAttribute("email", email);
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(EMAIL_PARAM_NAME,  email);		

		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);
		rv.setUrl("login/accountCreatedSuccess.jsp");
		return rv;

	}


	private String validatePassword(String password,
			AuthenticationPolicy settings) 
	{
		List<String> blackList = settings.getPasswordBlackList();
		if(blackList != null)
		{
			for(String forbidenPswd : blackList)
			{
				if(password.equalsIgnoreCase(forbidenPswd))
				{
					return PASSWORD_CANNOT_BE_USED;
				}
			}
		}

		
		if(password.length() > settings.getPasswordMaxLength())
		{
			return PASSWORD_IS_TOO_LONG;
		}

		if(password.length() < settings.getPasswordMinLength())
		{
			return PASSWORD_IS_TOO_SHORT;
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

		String retVal = oAuthConstants.OK;
		
		if(uppersCounter < settings.getPasswordMinUpCaseLetters())
		{
			retVal = formatter.format(PASSWORD_TOO_FEW_UPPERS, settings.getPasswordMinUpCaseLetters()).toString() ;
		}
		if(lowersCounter < settings.getPasswordMinLoCaseLetters())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_LOWERS, settings.getPasswordMinLoCaseLetters()).toString();
		}
		if(numericCounter < settings.getPasswordMinNumbers())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_NUMERICS, settings.getPasswordMinNumbers()).toString();
		}
		if(specialSymbolCounter < settings.getPasswordMinSpecialSymbols())
		{
			retVal =  formatter.format(PASSWORD_TOO_FEW_SPECIAL_SYMBOLS, settings.getPasswordMinSpecialSymbols()).toString();
		}
		
		formatter.close();
				
		return retVal;
	}


	private String encodeString(String email, String stringToEncode) 
	{
		//encoding the password:
        String encodedPassword = passwordEncoder.encodePassword(stringToEncode, email);	//the email is the salt
		return encodedPassword;
	}
	
	
	private void addPasswordConstraintsToResponse(HttpServletResponse response)
	{
		try
		{
			PrintWriter writer = response.getWriter();
			writer.println(oAuthConstants.OK + DELIMITER);
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
				"PasswordMinLoCaseLetters="+settings.getPasswordMinLoCaseLetters()+ DELIMITER +
				"PasswordMinNumbers="+settings.getPasswordMinNumbers()+ DELIMITER +
				"PasswordMinSpecialSymbols="+settings.getPasswordMinSpecialSymbols()+ DELIMITER +
				"PasswordMinUpCaseLetters="+settings.getPasswordMinUpCaseLetters()
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
	protected void forgotPasswordPage(	
			@RequestParam(EMAIL_PARAM_NAME) String email,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();

		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = flowsProcessor.isAccountLocked(email);
		if( accountState != AccountState.OK )
		{
			//account has been locked: do not check the user's answer, but notify user:
			writer.println(ERR_MSG + DELIMITER + ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);
			return;
		}

	    flowsProcessor.sendPasswordRestoreMail(email);

		writer.println(oAuthConstants.OK + DELIMITER + AN_EMAIL_WAS_SENT_TO_THE_GIVEN_ADDRESS_CLICK_ON_THE_LINK_THERE);
		//TODO: UI, instead of showing "secret Q" screen, show somethink like "an email has been sent"

	}

	
	/**
	 * (2)
	 * user clicks on the link in the "forgot password" email, and gets here.
	 *  
	 * @param email
	 * @param encUserAndTimestamp
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/login/forgotPasswordLinkCallback")
	protected View forgotPasswordLinkCallback(
			@RequestParam(oAuthConstants.HASH_PARAM_NAME) String encUserAndTimestamp,
			@RequestParam(oAuthConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		ImmutablePair<Date, String> stringAndDate = null;
		try
		{
			stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		}
		catch(CryptoException cryptoEx)
		{
			log.error("link is invalid; exception message: " + cryptoEx.getMessage());
			RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/index.htm?dt=psnc"		//psnc = password not changed
					);
			return irv;		
		}
		
		//check expiration:
		boolean expired = (System.currentTimeMillis() - stringAndDate.getLeft().getTime()) > (oAuthServerProperties.getLinksExpirationMinutes() * 1000 * 60L);
		if(expired)
		{
			log.error("link has expired");

			//adding attributes to the redirect return value:
			RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/index.htm?dt=psnc"		//psnc = password not changed
					);
			
			return irv;
		}

		//after all the checks, all look good (link not expired, etc). so show the user the "set new password" page.
		//if "secret question" is implemented, here you get the secret Q and show the user the screen to answer it. then
		//check the answer, etc.  


		RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/setNewPassword.htm"
				+ "&"
				+ oAuthConstants.HASH_PARAM_NAME 
				+ "=" + encUserAndTimestamp );

		return irv;
	}
	

	/**********************************************************************************************************/
	@Deprecated
	@RequestMapping("/login/setNewPasswordPage")
	protected View setNewPasswordPage(@RequestParam(EMAIL_PARAM_NAME) String email,
			@RequestParam(oAuthConstants.HASH_PARAM_NAME) String encUserAndTimestamp,
			HttpServletRequest request) throws Exception
	{
		ImmutablePair<Date, String> stringAndDate = null;
		try
		{
			stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		}
		catch(CryptoException cryptoEx)
		{
			log.error("link is invalid; exception message: " + cryptoEx.getMessage());
			RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/index.htm?dt=psnc");		//psnc = password not changed
			return irv;		
		}
		
		//check expiration:
		boolean expired = (System.currentTimeMillis() - stringAndDate.getLeft().getTime()) > (oAuthServerProperties.getLinksExpirationMinutes() * 1000 * 60L);
		if(expired)
		{
			log.error("link has expired");

			//adding attributes to the redirect return value:
			RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/index.htm?dt=psnc");		//psnc = password not changed
			
			return irv;
		}


		if( ! stringAndDate.getRight().equals(email))
		{
			log.error("signed email is different than the email parameter");

			RedirectView irv = new RedirectView("/" + oAuthConstants.OAUTH_WEB_APP_NAME + "/login/index.htm?dt=psnc");		//psnc = password not changed
			return irv;
		}

		return new InternalResourceView("/login/index.htm");
	}





	/**
	 *  (5)
	 *  
	 * @param email
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/setNewPassword")
	protected void setNewPassword( 
			@RequestParam(oAuthConstants.HASH_PARAM_NAME) String encUserAndTimestamp,
			@RequestParam("password") String password,
			HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();

		//validations: (using Fiddlr, hacker can hack this URL *AFTER* changing password to himself, and 
		//renaming the user to someone else.
		ImmutablePair<Date, String> stringAndDate = null;
		try
		{
			stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		}
		catch(CryptoException cryptoEx)
		{
			log.error("link is invalid; exception message: " + cryptoEx.getMessage());
			writer.println(ERR_MSG + DELIMITER + "link is invalid; exception message: " + cryptoEx.getMessage());
			return;
		}
		
		//check expiration:
		boolean expired = (System.currentTimeMillis() - stringAndDate.getLeft().getTime()) > (oAuthServerProperties.getLinksExpirationMinutes() * 1000 * 60L);
		if(expired)
		{
			log.error("link has expired");
			writer.println(ERR_MSG + DELIMITER + "link has expired");
			return;
		}


		String email = stringAndDate.getRight();

		//after validations, make the work: validate password constraints, and update DB:

		String encodedPassword = encodeString(email, password);

		//validate the input:
		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();

		String passwordValidityMsg = validatePassword(password, settings);
		if( !passwordValidityMsg.equals(oAuthConstants.OK) )
		{
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( SETTING_A_NEW_PASSWORD_HAS_FAILED_PLEASE_NOTE_THE_PASSWORD_POLICY_AND_TRY_AGAIN_ERROR_MESSAGE + passwordValidityMsg ));
			return;
		}

		//use API to go to the DB and update the password, and activate the account:
		flowsProcessor.setPassword(email, encodedPassword);

		writer.println(oAuthConstants.OK);
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
								@RequestParam(oAuthConstants.ENCRYPTED_USERNAME_PARAM_NAME) String encUser,
								HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();
		
		String email = cryptoService.extractString(encUser);
		

		// we need to check is account locked?! (for hackers...)
		//if account is already locked, no need to ask the user the secret question:
		AccountState accountState = flowsProcessor.isAccountLocked(email);
		if( accountState != AccountState.OK )
		{
			//account has been locked: do not check the user's answer, but notify user:
			writer.println(ERR_MSG + DELIMITER + ACCOUNT_LOCKED_OR_DOES_NOT_EXIST);
			return;
		}

		//validate the input:
		AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();

		String passwordValidityMsg = validatePassword(newPassword, settings);
		
		if( !passwordValidityMsg.equals(oAuthConstants.OK) )
		{
			//UI will redirect back to createAccount page, with error message:
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_NOT_COMPLIANT_WITH_POLICY + 
					" Error message: " + passwordValidityMsg) );
			return;
		}

		
		if(currentPassword.equals(newPassword))
		{
			writer.println(ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD ));
			return;
			
		}
		
		String encodedCurrentPassword = encodeString(email, currentPassword);
		String encodedNewPassword = encodeString(email, newPassword);

		//use API to go to the DB, validate current pswd and update the new one, and activate the account:
		Pair<String, String> retVal = flowsProcessor.changePassword(email, encodedCurrentPassword, encodedNewPassword);
		if( ! retVal.getLeft().equals(oAuthConstants.OK))
		{
			String errorText = retVal.getRight();

			log.error(errorText);
			
			//error - old password is incorrect; redirect back to same page (with the email as param):
			writer.println(ERR_MSG + DELIMITER + unescapeJaveAndEscapeHtml( PASSWORD_IS_INCORRECT ));

			return;
			
		}
		

		writer.println(oAuthConstants.OK);

	}
	/**********************************************************************************************************/

	
	@RequestMapping("/createAccountSuccessfully")
	protected View createAccountSuccessfully(HttpServletRequest request) throws Exception
	{
		return new InternalResourceView("/login/successfullyAuth.html");
	}
	

	@RequestMapping("/isAccountLocked")
	protected void isAccountLocked(@RequestParam(EMAIL_PARAM_NAME) String email, 
			HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();
		
		String isLockedStr = "false";
		
		AccountState accountState = flowsProcessor.isAccountLocked(email);
		if( accountState == AccountState.LOCKED )
		{
			isLockedStr = "true";
		}

		//account has been locked: send email and redirect to notify user:
		writer.println(oAuthConstants.OK + DELIMITER + isLockedStr);
	}

	private static String unescapeJaveAndEscapeHtml(String input)
	{
		String tmp = StringEscapeUtils.unescapeJava( input );
		return StringEscapeUtils.escapeHtml( tmp );
	}
	
}
