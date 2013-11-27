package com.ohadr.auth_flows.mocks;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AuthenticationUser;



public class AuthenticationAccountRepositoryMock extends AbstractAuthenticationAccountRepository 
{

	@Override
	public void createAccount(String email, String encodedPassword)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public AuthenticationUser getUser(String email) 
	{
		AuthenticationUser user = new AuthenticationUser();
		
		return user;
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
	public AuthenticationPolicy getAuthenticationPolicy() 
	{
		AuthenticationPolicy policy = new AuthenticationPolicy();
		policy.setMaxPasswordEntryAttempts( 5 );
		policy.setPasswordMaxLength( 8 );
		policy.setRememberMeTokenValidityInDays( 30 );

		return policy;
	}

}
