package com.ohadr.auth_flows.mocks;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;
import com.ohadr.auth_flows.types.AuthenticationUser;


@Component
public class InMemoryAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository 
{
	private Map<String, AuthenticationUser> users = new HashMap<String, AuthenticationUser>();

	@Override
	public AccountState createAccount(String email, String encodedPassword)
	{
		if( getUser(email) != null )
		{
			return AccountState.ALREADY_EXIST;
		}
		else
		{
			AuthenticationUser user = new AuthenticationUser();
			user.setEmail(email);
			user.setPassword(encodedPassword);
			user.setEnabled(false);
			
			users.put(email, user);
			
			return AccountState.OK;
		}		
	}

	@Override
	public AuthenticationUser getUser(String email) 
	{
		AuthenticationUser user = users.get(email);
		return user;
	}

	@Override
	public void deleteOAuthAccount(String email) 
	{
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
