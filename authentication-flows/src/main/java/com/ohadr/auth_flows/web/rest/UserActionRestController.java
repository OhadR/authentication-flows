package com.ohadr.auth_flows.web.rest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.auth_flows.web.AuthenticationFlowsException;
import com.ohadr.crypto.exception.CryptoException;

@Controller
public class UserActionRestController
{
	private static Logger log = Logger.getLogger(UserActionRestController.class);

	@Autowired
	private AuthenticationFlowsProcessor flowsProcessor;

	
	@RequestMapping("/rest/createAccount")
	final protected void createAccountRest(
			@RequestParam( FlowsConstatns.EMAIL_PARAM_NAME ) String email,
			@RequestParam("password") String password,
			@RequestParam( FlowsConstatns.CONFIRM_PASSWORD_PARAM_NAME ) String retypedPassword,
			@RequestParam( value = "firstName", required = false ) String firstName,
			@RequestParam( value = "lastName", required = false ) String lastName,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		log.debug( "/rest/createAccount: email=" + email +
				", firstName=" + firstName + 
				", lastName=" + lastName
				);
		PrintWriter writer = response.getWriter();

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
			
			writer.println(	FlowsUtil.unescapeJaveAndEscapeHtml( afe.getMessage() ) );

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
//        response.setContentType("text/html"); 
		response.setStatus(HttpServletResponse.SC_CREATED);
	}
	
	
//  @RequestMapping(value = "/order", method = RequestMethod.POST)
	@RequestMapping("/rest/forgotPassword")
	final protected void forgotPasswordRest(	
			@RequestParam( FlowsConstatns.EMAIL_PARAM_NAME ) String email,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		PrintWriter writer = response.getWriter();

		String serverPath = FlowsUtil.getServerPath(request);

		try
		{
			flowsProcessor.handleForgotPassword(email, serverPath);
		} 
		catch (AuthenticationFlowsException afe)
		{
			log.error( afe.getMessage() );

			writer.println( FlowsUtil.unescapeJaveAndEscapeHtml( afe.getMessage() ) );

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		//adding attributes to the redirect return value:
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	@RequestMapping("/rest/setNewPassword")
	protected void setNewPasswordRest( 
			@RequestParam(FlowsConstatns.HASH_PARAM_NAME) String encUserAndTimestamp,
			@RequestParam("password") String password,
			@RequestParam( FlowsConstatns.CONFIRM_PASSWORD_PARAM_NAME ) String retypedPassword,
			HttpServletResponse response) throws IOException
	{
		PrintWriter writer = response.getWriter();

		try
		{
			flowsProcessor.handleSetNewPassword(encUserAndTimestamp, password, retypedPassword);
		}
		catch(AuthenticationFlowsException afe)
		{		
			log.error( afe.getMessage() );

			writer.println( FlowsUtil.unescapeJaveAndEscapeHtml( afe.getMessage() ) );

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		catch(CryptoException cryptoEx)
		{
			log.error( cryptoEx.getMessage() );

			writer.println( FlowsUtil.unescapeJaveAndEscapeHtml( cryptoEx.getMessage() ) );

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
}