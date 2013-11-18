package com.ohadr.oauth_srv.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

@Service("authenticationSuccessHandler")
public class MyAuthenticationSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler 
	{

	public MyAuthenticationSuccessHandler()
	{
		// TODO Auto-generated constructor stub
	}
	
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException
    {
    	HttpSession session = request.getSession(false);
    	
    	if(session != null)
    	{
        	int interval = session.getMaxInactiveInterval();
        	System.out.println("session interval is " + interval);
        	session.setMaxInactiveInterval(50);
    	}
    	
    	super.onAuthenticationSuccess(request, response, authentication);
    }


}
