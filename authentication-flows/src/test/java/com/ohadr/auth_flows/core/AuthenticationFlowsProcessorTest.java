package com.ohadr.auth_flows.core;

import org.junit.Assert;
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
	public void testCreateAccountBadEmail()
	{
	   	ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet.xml");
	 
   	   	AuthenticationFlowsProcessor processor = (AuthenticationFlowsProcessor) context.getBean("authenticationFlowsProcessorImpl");
   	
		try
		{
			processor.createAccount("email", "pass", "pass", "firstName", "lastName", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			Assert.assertEquals("unexpected error return.", AuthenticationFlowsProcessorImpl.EMAIL_NOT_VALID, e.getMessage());
			return;
		}
		Assert.fail();		
	}

	@Test
	public void testCreateAccountUnequalPasswords()
	{
	   	ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet.xml");
	 
	   	AuthenticationFlowsProcessor processor = (AuthenticationFlowsProcessor) context.getBean("authenticationFlowsProcessorImpl");
   	
		try
		{
			processor.createAccount("email@ohadr.com", "pass1", "pass2", "firstName", "lastName", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			Assert.assertEquals("unexpected error return.", AuthenticationFlowsProcessorImpl.ACCOUNT_CREATION_HAS_FAILED_PASSWORDS_DO_NOT_MATCH, e.getMessage());
			return;
		}
		Assert.fail();		
	}

	/**
	 * this test checks the flow of trying to "create account" for an already existing account
	 * First, we create an account, without activating it. Then we try to create it again - it should work.
	 * Second, we activate the account and then retry to create it - this time it should fail.
	 */
	@Test
	public void testCreateAccount_CreateExistingAccount()
	{
	   	ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet.xml");
	 
	   	AuthenticationFlowsProcessor processor = (AuthenticationFlowsProcessor) context.getBean("authenticationFlowsProcessorImpl");
   	
	   	final String EMAIL = "email@ohadr.com";
		//create the account
	   	try
		{
			processor.createAccount(EMAIL, "pass", "pass", "firstName", "lastName", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			Assert.fail();		
		}

	   	//create the account again - the account was not activated, so this should be successful:
	   	try
		{
			processor.createAccount(EMAIL, "pass2", "pass2", "firstName2", "lastName2", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			Assert.fail();		
		}
	   	
	   	//now activate the account:
	   	processor.setEnabled(EMAIL);

	   	//create the account again - the account was not activated, so this should be successful:
	   	try
		{
			processor.createAccount(EMAIL, "pass2", "pass2", "firstName2", "lastName2", "path");
		} 
		catch (AuthenticationFlowsException e)
		{
			Assert.assertEquals("unexpected error return.", AuthenticationFlowsProcessorImpl.USER_ALREADY_EXIST, e.getMessage());
			return;
		}
		Assert.fail();		
	
	}
}
