package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.service.CryptoService;
import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.core.AuthenticationFlowsProcessorImpl;
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
	public static final String PASSWORD_IS_INCORRECT = "password is incorrect";
	public static final String USER_DOES_NOT_EXIST = "user does not exist";
//	public static final String BAD_EMAIL_PARAM = "Bad email param";

	public static final String CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD = "CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD";
	public static final String SECRET_ANSWER_CANNOT_CONTAIN_THE_PASSWORD_AND_VICE_VERSA = "Secret Answer cannot contain the password, and vice versa.";
	public static final String ACCOUNT_LOCKED_OR_DOES_NOT_EXIST = "Account is locked or does not exist";
	
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

		String path = FlowsUtil.getServerPath(request);

		
		try
		{
			flowsProcessor.createAccount(
					email, password, retypedPassword, path);
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
		
		
		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);
		rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "accountCreatedSuccess.jsp");
		return rv;

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

		if( email == null || email.isEmpty() )
		{
			//account has been locked/does not exist: notify user:
			log.error( AuthenticationFlowsProcessorImpl.EMAIL_NOT_VALID );

			attributes.put(FlowsConstatns.ERR_MSG,  AuthenticationFlowsProcessorImpl.EMAIL_NOT_VALID);		
			attributes.put(FlowsConstatns.ERR_HEADER,  AuthenticationFlowsProcessorImpl.EMAIL_NOT_VALID);
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "error.jsp");
			return rv;
		}
		
		
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
			flowsProcessor.validateRetypedPassword(password, retypedPassword);

			//validations: (using Fiddlr, hacker can hack this URL *AFTER* changing password to himself, and 
			//renaming the user to someone else.
			ImmutablePair<Date, String> stringAndDate = null;
			stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
			
			validateExpiration(stringAndDate.getLeft());

			email = stringAndDate.getRight();

			//after validations, make the work: validate password constraints, and update DB:

			//validate the input:
			AuthenticationPolicy settings = flowsProcessor.getAuthenticationSettings();

			flowsProcessor.validatePassword(password, settings);
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
		

		String encodedPassword = flowsProcessor.encodeString(email, password);

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
		

		try
		{
			flowsProcessor.validateRetypedPassword(newPassword, retypedPassword);
		}
		catch (AuthenticationFlowsException afe)
		{
//			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);		
			//adding attributes to the redirect return value:
//			rv.setAttributesMap(attributes);
//			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
//			return rv;
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + afe.getMessage());
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
			flowsProcessor.validatePassword(newPassword, settings);
		}
		catch(AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );

			//UI will redirect back to createAccount page, with error message:
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( afe.getMessage()) );
			return;
		}
		
		
		if(currentPassword.equals(newPassword))
		{
			writer.println(FlowsConstatns.ERR_MSG + DELIMITER + 
					unescapeJaveAndEscapeHtml( CHANGE_PASSWORD_FAILED_NEW_PASSWORD_SAME_AS_OLD_PASSWORD ));
			return;
			
		}
		
		String encodedCurrentPassword = flowsProcessor.encodeString(email, currentPassword);
		String encodedNewPassword = flowsProcessor.encodeString(email, newPassword);

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


	private void validateExpiration(Date linkCreationDate) throws AuthenticationFlowsException
	{
		boolean expired = (System.currentTimeMillis() - linkCreationDate.getTime()) > (properties.getLinksExpirationMinutes() * 1000 * 60L);
		if( expired )
		{
			throw new AuthenticationFlowsException(LINK_HAS_EXPIRED);
		}
	}
}
