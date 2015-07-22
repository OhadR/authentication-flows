package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.service.CryptoService;
import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AuthenticationFlowsException;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.FlowsConstatns;

@Controller
public class UserActionController
{
	public static final String PASSWORD_IS_INCORRECT = "password is incorrect";

	public static final String ACCOUNT_LOCKED_OR_DOES_NOT_EXIST = "Account is locked or does not exist";
	
	private static Logger log = Logger.getLogger( UserActionController.class );
		
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
			@RequestParam( FlowsConstatns.EMAIL_PARAM_NAME ) String email,
			@RequestParam("password") String password,
			@RequestParam( FlowsConstatns.CONFIRM_PASSWORD_PARAM_NAME ) String retypedPassword,
//			@RequestParam("secretQuestion") String secretQuestion,						NOT IMPLEMENTED
//			@RequestParam("secretQuestionAnswer") String secretQuestionAnswer,			NOT IMPLEMENTED
//			@RequestParam(FlowsConstatns.REDIRECT_URI_PARAM_NAME) String redirectUri,	NOT IMPLEMENTED
			@RequestParam( value = "firstName", required = false ) String firstName,
			@RequestParam( value = "lastName", required = false ) String lastName,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		PrintWriter writer = response.getWriter();

		RedirectView rv = new RedirectView();

//		request.setAttribute("email", email);
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(FlowsConstatns.EMAIL_PARAM_NAME,  email);		

		String path = FlowsUtil.getServerPath(request);

		
		try
		{
			flowsProcessor.createAccount(
					email, password, retypedPassword, 
					firstName, lastName, 
					path);
		} 
		catch (AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );
			
			writer.println(FlowsConstatns.ERR_MSG + FlowsConstatns.DELIMITER + 
					FlowsUtil.unescapeJaveAndEscapeHtml( afe.getMessage() ) );


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
			writer.println(FlowsConstatns.OK + FlowsConstatns.DELIMITER);
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
				"PasswordMaxLength="+ settings.getPasswordMaxLength()+ FlowsConstatns.DELIMITER +
				"PasswordMinLength="+settings.getPasswordMinLength()+ FlowsConstatns.DELIMITER +
				"PasswordMinLoCaseLetters="+settings.getPasswordMinLoCaseChars()+ FlowsConstatns.DELIMITER +
				"PasswordMinNumbers="+settings.getPasswordMinNumbericDigits()+ FlowsConstatns.DELIMITER +
				"PasswordMinSpecialSymbols="+settings.getPasswordMinSpecialSymbols()+ FlowsConstatns.DELIMITER +
				"PasswordMinUpCaseLetters="+settings.getPasswordMinUpCaseChars()
				;


		builder.append( FlowsConstatns.DELIMITER		//we sepoarate the secretQ's and the constraints with '|'. the last secretQ comes with a single '|' after it, so now add another one:
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
			@RequestParam( FlowsConstatns.EMAIL_PARAM_NAME ) String email,
			HttpServletRequest request) throws Exception
	{
		RedirectView rv = new RedirectView();

//		request.setAttribute("email", email);
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put( FlowsConstatns.EMAIL_PARAM_NAME,  email );
		
		String serverPath = FlowsUtil.getServerPath(request);
		try
		{
			flowsProcessor.handleForgotPassword(email, serverPath);
		} 
		catch (AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );

			attributes.put(FlowsConstatns.ERR_MSG,  afe.getMessage());		
			attributes.put(FlowsConstatns.ERR_HEADER,  afe.getMessage());
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "error.jsp");
			return rv;
		}
		

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
			@RequestParam( FlowsConstatns.CONFIRM_PASSWORD_PARAM_NAME ) String retypedPassword)
	{
		RedirectView rv = new RedirectView();
		Map<String, String> attributes = new HashMap<String, String>();
		String email;

		try
		{
			email = flowsProcessor.handleSetNewPassword(encUserAndTimestamp, password, retypedPassword);
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
			log.error( cryptoEx.getMessage() );

			attributes.put(FlowsConstatns.ERR_MSG,  cryptoEx.getMessage() );		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
			return rv;
		}
		

		attributes.put(FlowsConstatns.EMAIL_PARAM_NAME,  email);
		attributes.put(FlowsConstatns.BASE_URL_PATH, properties.getBaseUrlPath());
		attributes.put(FlowsConstatns.LOGIN_URL_SUCCESS, properties.getLoginSuccessEndpointUrl());
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
	protected View changePassword( 
								@RequestParam("currentPassword") String currentPassword,
								@RequestParam("newPassword") String newPassword,
								@RequestParam( FlowsConstatns.CONFIRM_PASSWORD_PARAM_NAME ) String retypedPassword,
								//@RequestParam(FlowsConstatns.ENCRYPTED_USERNAME_PARAM_NAME) String encUser,
								HttpServletResponse response) throws Exception
	{
		String email="";
		RedirectView rv = new RedirectView();
		PrintWriter writer = response.getWriter();
		Map<String, String> attributes = new HashMap<String, String>();

		try
		{
			email=flowsProcessor.handleChangePassword(currentPassword, newPassword, retypedPassword);
		}
		catch (AuthenticationFlowsException afe)
		{
//			attributes.put(FlowsConstatns.ERR_MSG,  ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH);		
			//adding attributes to the redirect return value:
//			rv.setAttributesMap(attributes);
//			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "setNewPassword.jsp");
//			return rv;
			log.error( afe.getMessage() );

			//UI will redirect back to createAccount page, with error message:
			writer.println(FlowsConstatns.ERR_MSG + FlowsConstatns.DELIMITER + 
					FlowsUtil.unescapeJaveAndEscapeHtml( afe.getMessage()) );
			
			attributes.put(FlowsConstatns.ERR_MSG,  afe.getMessage());		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.SECURE_FORMS_DIR +"/" + "changePassword.jsp");

			return rv;
		}


		writer.println(FlowsConstatns.OK);
		attributes.put(FlowsConstatns.EMAIL_PARAM_NAME,  email);
		attributes.put(FlowsConstatns.BASE_URL_PATH, properties.getBaseUrlPath());
		attributes.put(FlowsConstatns.LOGIN_URL_SUCCESS, properties.getLoginSuccessEndpointUrl());
		//adding attributes to the redirect return value:
		rv.setAttributesMap(attributes);		
		rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "passwordSetSuccess.jsp");
		return rv;
	}
	/**********************************************************************************************************/

	
	protected void isAccountLocked(@RequestParam( FlowsConstatns.EMAIL_PARAM_NAME ) String email, 
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
		writer.println(FlowsConstatns.OK + FlowsConstatns.DELIMITER + isLockedStr);
	}
}
