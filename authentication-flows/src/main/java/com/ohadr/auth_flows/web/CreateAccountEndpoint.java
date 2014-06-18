package com.ohadr.auth_flows.web;


/**
 * this class let applications override this impl and add their custom functionality
 */
public class CreateAccountEndpoint 
{
	/**
	 * any other additional validations the app does before account creation. upon failure, exception is thrown.
	 * @param email
	 * @param password
	 * @throws AuthenticationFlowsException
	 */
	public void additionalValidations(String email, String password) throws AuthenticationFlowsException 
	{
	}
}
