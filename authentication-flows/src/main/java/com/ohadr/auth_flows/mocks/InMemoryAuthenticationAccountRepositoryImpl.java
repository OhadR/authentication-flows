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
	private Map<String, AuthenticationUser> users = new HashMap<String, AuthenticationUser>();

	
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
		AuthenticationUser user = users.get(username);
		return user;
	}

	
	
	@Override
	public AccountState createAccount(String email, String encodedPassword,
			int numLoginAttemptsAllowed)
	{
		if( loadUserByUsername(email) != null )
		{
			return AccountState.ALREADY_EXIST;
		}
		else
		{
			AuthenticationUser user = new InMemoryAuthenticationUserImpl(email,
					encodedPassword,
					false,
					numLoginAttemptsAllowed);

			user.setPasswordLastChangeDate( new Date(System.currentTimeMillis()) );
			
			users.put(email, user);
			
			return AccountState.OK;
		}		
	}

	@Override
	public void deleteAccount(String username) 
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
					username, newEncodedPassword, true, storedUser.getLoginAttemptsLeft());
			newUser.setPasswordLastChangeDate(new Date( System.currentTimeMillis() ));

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
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
					storedUser.getLoginAttemptsLeft());

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
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
					storedUser.getLoginAttemptsLeft());
			
			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteAccount(username);
			users.put(username, newUser);
		}
	}



	@Override
	public void createUser(UserDetails user)
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void updateUser(UserDetails user)
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void deleteUser(String username)
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
