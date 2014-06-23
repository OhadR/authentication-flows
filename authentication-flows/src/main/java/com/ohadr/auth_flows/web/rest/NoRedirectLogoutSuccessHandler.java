package com.ohadr.auth_flows.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * this class is needed in order to support REST logout - without redirection after the successful logout.
 * Authentication should return 200 instead of 301. 
 * see: http://www.baeldung.com/2011/10/31/securing-a-restful-web-service-with-spring-security-3-1-part-3/
 * http://www.codeproject.com/Tips/521847/Logout-Spring-s-LogoutFilter
 * @author OhadR
 *
 */
public class NoRedirectLogoutSuccessHandler implements LogoutSuccessHandler
{
	private static Logger log = Logger.getLogger(NoRedirectLogoutSuccessHandler.class);

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException
	{
		// no redirect !! (unlike @SimpleUrlLogoutSuccessHandler, that redirects after logout)		
		log.info( "NoRedirectLogoutSuccessHandler" );
	}
}
