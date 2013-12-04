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
			AuthenticationUser user = new InMemoryAuthenticationUserImpl(email,
					encodedPassword,
					false);

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
	public void deleteAccount(String username) 
	{
		users.remove(username);
	}
	
	
	@Override
	public boolean setPassword(String email, String newPassword)
	{
		return changePassword(email, newPassword);
	}


	@Override
	public boolean changePassword(String username, String newEncodedPassword) 
	{
		AuthenticationUser storedUser = getUser(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, newEncodedPassword, true);
			newUser.setPasswordLastChangeDate(new Date( System.currentTimeMillis() ));

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
			users.put(username, newUser);
			return true;
		}
		else
		{
			return false;
		}
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
	public void setEnabled(String username) 
	{
		setEnabledFlag(username, true);
	}

	@Override
	public void setDisabled(String username) 
	{
		setEnabledFlag(username, false);
	}



	@Override
	protected void setEnabledFlag(String username, boolean flag) 
	{
		AuthenticationUser storedUser = getUser(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, storedUser.getPassword(), flag);

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
			users.put(username, newUser);
		}
	}



	@Override
	protected void updateLoginAttemptsCounter(String username, int attempts) 
	{
		AuthenticationUser storedUser = getUser(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, storedUser.getPassword(),
					storedUser.isEnabled());
			newUser.setLoginAttemptsCounter(attempts);
			
			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
			users.put(username, newUser);
		}
	}

}
