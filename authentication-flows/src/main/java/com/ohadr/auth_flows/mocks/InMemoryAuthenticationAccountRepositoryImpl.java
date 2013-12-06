package com.ohadr.auth_flows.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ohadr.auth_flows.core.AbstractAuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationPolicy;


//@Component
public class InMemoryAuthenticationAccountRepositoryImpl extends AbstractAuthenticationAccountRepository 
{
	private Map<String, UserDetails> users = new HashMap<String, UserDetails>();

	
	public InMemoryAuthenticationAccountRepositoryImpl()
	{
		createAccount("ohad@ohadr.com", "aaaa", 22);		
	}

	

	/**
	 * implementations for {@link org.springframework.security.core.userdetails.UserDetailsService}, 
	 * with its <code>loadUserByUsername(String username)</code>
	 */
	@Override
	public AuthenticationUser loadUserByUsername(String username)
			throws UsernameNotFoundException 
	{
		AuthenticationUser user = (AuthenticationUser) users.get(username);
		return user;
	}

	
	
	@Override
	public AccountState createAccount(String username, String encodedPassword,
			int numLoginAttemptsAllowed)
	{
		UserDetails user = new InMemoryAuthenticationUserImpl(username,
				encodedPassword,
				false,
				numLoginAttemptsAllowed,
				new Date(System.currentTimeMillis()));
		
		if( loadUserByUsername(username) != null )
		{
			return AccountState.ALREADY_EXIST;
		}
		else
		{
			createUser(user);
			
			return AccountState.OK;
		}		
	}

	@Override
	public void createUser(UserDetails user)
	{
		users.put(user.getUsername(), user);
	}

	@Override
	public void deleteUser(String username)
	{
		users.remove(username);
	}

	
	@Override
	public void setPassword(String email, String newPassword)
	{
		changePassword(email, newPassword);
	}


	@Override
	public void changePassword(String username, String newEncodedPassword) 
	{
		AuthenticationUser storedUser = loadUserByUsername(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, newEncodedPassword, true, storedUser.getLoginAttemptsLeft(),
					new Date(System.currentTimeMillis()) );

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteUser(username);
			users.put(username, newUser);
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
		AuthenticationUser storedUser =  loadUserByUsername(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, 
					storedUser.getPassword(), 
					flag,
					storedUser.getLoginAttemptsLeft(),
					storedUser.getPasswordLastChangeDate());

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteUser(username);
			users.put(username, newUser);
		}
	}



	@Override
	protected void updateLoginAttemptsCounter(String username, int attempts) 
	{
		AuthenticationUser storedUser =  loadUserByUsername(username);
		if(storedUser != null)
		{
			AuthenticationUser newUser = new InMemoryAuthenticationUserImpl(
					username, 
					storedUser.getPassword(),
					storedUser.isEnabled(),
					storedUser.getLoginAttemptsLeft(),
					storedUser.getPasswordLastChangeDate());
			
			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteUser(username);
			users.put(username, newUser);
		}
	}



	@Override
	public void updateUser(UserDetails user)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean userExists(String username)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
