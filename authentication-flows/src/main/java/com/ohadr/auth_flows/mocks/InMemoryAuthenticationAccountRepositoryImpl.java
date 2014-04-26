package com.ohadr.auth_flows.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.acls.model.AlreadyExistsException;
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
	public void createUser(UserDetails user)
	{
		AuthenticationUser authUser = (AuthenticationUser) user;

		UserDetails newUser = new InMemoryAuthenticationUserImpl(authUser.getUsername(),
				authUser.getPassword(),
				false,
				authUser.getLoginAttemptsLeft(),
				new Date(System.currentTimeMillis()),
				user.getAuthorities());
		
		if( loadUserByUsername( newUser.getUsername() ) != null )
		{
			//ALREADY_EXIST:
			throw new AlreadyExistsException("user laready exists");
		}

		users.put(newUser.getUsername(), newUser);
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
					new Date(System.currentTimeMillis()),
					storedUser.getAuthorities());

			//delete old user and set a new one, since iface does not support "setPassword()":
			deleteUser(username);
			users.put(username, newUser);
		}
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
					storedUser.getPasswordLastChangeDate(),
					storedUser.getAuthorities());

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
					storedUser.getPasswordLastChangeDate(),
					storedUser.getAuthorities());
			
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
