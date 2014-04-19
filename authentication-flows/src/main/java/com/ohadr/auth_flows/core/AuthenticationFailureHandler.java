package com.ohadr.auth_flows.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.FlowsConstatns;

// nice and intersting reference: 
// https://code.google.com/p/springas-train-example/source/browse/trunk/serverIntegration/server/common/src/main/java/cn/com/oceansoft/flex4/server/common/interceptor/CustomAuthenticationFailureHandler.java?r=73&spec=svn73

public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler 
{
	public AuthenticationFailureHandler(String defaultFailureUrl)
	{
		super(defaultFailureUrl);
	}


	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
	public static final String DEFAULT_ACCOUNT_LOCKED_PAGE = FlowsConstatns.LOGIN_FORMS_DIR +"/" + "accountLocked.htm";
	
    private static Logger log = Logger.getLogger(AuthenticationFailureHandler.class);
	
	@Autowired
	private AuthenticationFlowsProcessor processor;

	private String accountLockedUrl = DEFAULT_ACCOUNT_LOCKED_PAGE;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException
	{
	    //if account is already locked, we do not need to re-send the email and re-lock. so just delegate to parent
	    if( exception instanceof LockedException )
	    {
	    	super.onAuthenticationFailure(request, response, exception);
	    	return;
	    }

	    String username = 
//			exception.getAuthentication().getName();		//deprecated!
			obtainUsername(request);
	    log.info("login failed for user: " + username);

	    //notify the processor (that updates the DB):
	    processor.setLoginFailureForUser(username);
	    AccountState state = processor.getAccountState(username);

	    if( state == AccountState.LOCKED )
		{
		    log.info("Account has been locked out for user: " + username + " due to exceeding number of attempts to login.");

			String path = FlowsUtil.getServerPath(request);
		    processor.sendUnlockAccountMail(username, path);
		    
		    //redirect the user to "account has been locked" page:
		    getRedirectStrategy().sendRedirect(request, response, accountLockedUrl);
		}
	    else
	    {
	    	super.onAuthenticationFailure(request, response, exception);
	    }
	    
	}

	public void setAccountLockedUrl(String accountLockedUrl) 
	{
        this.accountLockedUrl = accountLockedUrl;
    }
	
    protected String obtainUsername(HttpServletRequest request) 
    {
        return request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
    }

}
