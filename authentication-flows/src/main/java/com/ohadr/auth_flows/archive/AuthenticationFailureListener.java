package com.ohadr.auth_flows.archive;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AccountState;

//@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> 
{
	@Autowired
	private AuthenticationFlowsProcessor processor;

	private static Logger log = Logger.getLogger(AuthenticationFailureListener.class);

	@Override
	public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent ev) 
	{
	    String username = ev.getAuthentication().getName();
	    log.info("login failed for user: " + username);

	    //notify the processor (that updates the DB):
	    processor.setLoginFailureForUser(username);
	    AccountState state = processor.getAccountState(username);

	    if( state == AccountState.LOCKED )
		{
		    log.info("Account has been locked out for user: " + username + " due to exceeding number of attempts to login.");
			//account has been locked: send email and redirect-uri to notify user:
		    //NOTE: we don't have the redirect-uri, so we send empty str, and the consequence is that 
		    //after the user will login, he will be redirected to '/documents'
		    processor.sendUnlockAccountMail(username, "");
		}
	    
	}

}
