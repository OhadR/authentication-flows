package com.ohadr.auth_flows.core;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.AuthenticationFlowsException;

public class AuthenticationFlowsProcessorTest
{
//	@Autowired
	AuthenticationFlowsProcessor processor;
	
	@Test
	public void testCreateAccount()
	{
	   	ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet.xml");
	 
   	
	   	AuthenticationFlowsProcessor processor = (AuthenticationFlowsProcessor) context.getBean("authenticationFlowsProcessorImpl");

   	
		try
		{
			processor.createAccount("email", "password", "Password", "firstName", "lastName", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
