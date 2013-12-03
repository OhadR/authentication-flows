package com.ohadr.auth_flows.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


//@Component
public class InMemoryAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository 
{
	private Map<String, AuthenticationUser> users = new HashMap<String, AuthenticationUser>();

	
	public InMemoryAuthenticationAccountRepositoryImpl()
	{
		createAccount("ohad@ohadr.com", "aaaa");		
	}
	
	
	
	@Override
	public AccountState createAccount(String email, String encodedPassword)
	{
		if( getUser(email) != null )
		{
			return AccountState.ALREADY_EXIST;
		}
		else
		{
			AuthenticationUser user = new InMemoryAuthenticationUserImpl();
			user.setEmail(email);
			user.setPassword(encodedPassword);
			user.setActivated(false);
			user.setPasswordLastChangeDate( new Date(System.currentTimeMillis()) );
			
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
	
	@Override
	public void setEnabled(String email) 
	{
		AuthenticationUser user = getUser(email);
		user.setActivated(true);
	}

}
