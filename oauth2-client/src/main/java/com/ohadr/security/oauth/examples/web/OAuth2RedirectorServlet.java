package com.ohadr.security.oauth.examples.web;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//import com.watchdox.web.auth.WebAppOAuth2ResourceDetails;

/**
 *	this servlet responds to /oauth/commence
 */
@Controller
public class OAuth2RedirectorServlet
{
	@Autowired
	private OAuth2RestTemplate watchdoxWebappRestTemplate;

//	private static final Logger logger = Logger.getLogger(OAuth2RedirectorServlet.class);

	private AuthenticationSuccessHandler authenticationSuccessHandler = new SavedRequestAwareAuthenticationSuccessHandler();
	

	@RequestMapping("/oauth/commence")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
//		BaseOAuth2ProtectedResourceDetails resource = new ImplicitResourceDetails();
//		resource.setId(WebAppOAuth2ResourceDetails.WATCHDOX_OAUTH2_RESOURCE_ID);

		
		OAuth2AccessToken token = null;

//        String demo = watchdoxWebappRestTemplate.getForObject(URI.create("www.google.com"), String.class);

//			logger.info("OAuth2 external authentication");


//		token = watchdoxWebappRestTemplate.getAccessToken();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	//		if (token != null)
			{
//				authentication = tokenServices.loadAuthentication(token.getValue());

				if (authentication == null)
				{
					SecurityContextHolder.getContext().setAuthentication(null);
//					OAuth2ClientContextHolder.getContext().removeAccessToken(resource);
					throw new InvalidTokenException("Invalid token: " + token.getValue());
				}

				SecurityContextHolder.getContext().setAuthentication(authentication);

				getAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
			}
			
		
	}

	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler)
	{
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	public AuthenticationSuccessHandler getAuthenticationSuccessHandler()
	{
		return authenticationSuccessHandler;
	}
}
