package com.ohadr.auth_flows.core;

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

import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.crypto.service.CryptoService;

@Service("authenticationSuccessHandler")
public class AuthenticationSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler
{
	@Autowired
	private AuthenticationFlowsProcessor processor;

	@Autowired
	private CryptoService cryptoService;

	private static Logger log = Logger.getLogger(AuthenticationSuccessHandler.class);

	public AuthenticationSuccessHandler()
	{
	}
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException
    {
	    String username = authentication.getName();
	    log.info("login success for user: " + username);

	    //TODO: there is another way for this mechanism:
	    //notify the API-client, that notifies the API (that updates the DB):
	    boolean passChangeRequired = processor.setLoginSuccessForUser(username);
		if(passChangeRequired)
		{
			//start "change password" flow:
			log.info("password expired for user " + username);
			String encUser = cryptoService.generateEncodedString(username);
			//redirect to a set new password page:
			response.sendRedirect("/login/changePassword.htm?username=" + username + 
					"&" + FlowsConstatns.HASH_PARAM_NAME + "=" + encUser + "&dt=cp");
			
			return;

		}

		
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
