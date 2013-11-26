package com.ohadr.auth_flows.core;

import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AuthenticationUser;


//@Repository
public class AuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository 
{

	@Override
	public void createAccount(String email, String encodedPassword
			//NOT IMPLEMENTED: String secretQuestion, String encodedAnswer
			)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AuthenticationUser getUser(String email) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteOAuthAccount(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean changePassword(String username, String newEncodedPassword) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AuthenticationPolicy getAuthenticationPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

}
