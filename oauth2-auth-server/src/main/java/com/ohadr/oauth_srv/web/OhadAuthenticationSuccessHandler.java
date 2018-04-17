package com.ohadr.oauth_srv.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import com.ohadr.crypto.service.CryptoService;

@Service("authenticationSuccessHandler")
public class OhadAuthenticationSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler
{
	private static final String OAUTH_WEB_APP_NAME = "oauth-srv";

	@Autowired
	private CryptoService cryptoService;
	
	private static Logger log = Logger.getLogger(AuthenticationSuccessEventListener.class);

	public OhadAuthenticationSuccessHandler()
	{
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException
    {
	    String username = authentication.getName();
	    log.info("login success for user: " + username);

		/////////////////////////////////////////
		// changeSessionTimeout(request);
		/////////////////////////////////////////

		
    	super.onAuthenticationSuccess(request, response, authentication);
    }
    
    private void changeSessionTimeout(HttpServletRequest request)
    {
		HttpSession session = request.getSession(false);
    	
    	int interval = session.getMaxInactiveInterval();
    	System.out.println("session interval is " + interval);
    	session.setMaxInactiveInterval(50);
    	
    }

}