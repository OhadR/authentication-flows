package com.watchdox.oauth_srv.web;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;


@Component
public class AuthenticationSuccessEventListener implements
		ApplicationListener<AuthenticationSuccessEvent>
{
	private static Logger log = Logger.getLogger(AuthenticationSuccessEventListener.class);

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent ev)
	{
	    String username = ev.getAuthentication().getName();
	    log.info("login success for user: " + username);

	    //Do Something with the knowledge the login was successfull...
	}

}
