package com.ohadr.auth_flows.core;


import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.interfaces.AuthenticationUser;
import com.ohadr.auth_flows.types.AccountState;


public abstract class AbstractAuthenticationAccountRepository implements AuthenticationAccountRepository
{
	protected abstract void setEnabledFlag(String email, boolean flag) throws NoSuchElementException; 
	protected abstract void updateLoginAttemptsCounter(String email, int attempts) throws NoSuchElementException; 
	
	public AbstractAuthenticationAccountRepository()
	{
		System.out.println(this.getClass().getName() + " created");
	}
	

	@Override
	public boolean isActivated(String email) 
	{
		UserDetails user = loadUserByUsername(email);
		return user.isEnabled();
	}


	@Override
	public void decrementAttemptsLeft(String email)
	{
		AuthenticationUser user = (AuthenticationUser) loadUserByUsername(email);
		int attempts = user.getLoginAttemptsLeft();
		updateLoginAttemptsCounter(email, --attempts);
	}

	//TODO: impl correct only for in-mem, not jdbc
	@Override
	public void setAttemptsLeft(String email, int numAttemptsAllowed)
	{
		AuthenticationUser user = (AuthenticationUser) loadUserByUsername(email);
		
		//user might be null since we "login-success" once to the user account, and then to the client-application (oAuth mechanism)
		//so if 'email' is the "client app", there will be no 'user' and it will be null:
		if(user != null)
		{
			updateLoginAttemptsCounter( email, numAttemptsAllowed );
		}	
		
	}

	/**
	 * account is locked only if the enable is false AND attempts cntr is NOT 0. o/w, if counter is 0, we deal with 
	 * not-activated account.
	 */
	@Override
	public AccountState isAccountLocked(String email) 
	{
		AuthenticationUser user = null;
		try
		{
			user = (AuthenticationUser) loadUserByUsername(email);
		}
		catch(UsernameNotFoundException unfe)
		{
			//user does not exist:
			return AccountState.NOT_EXIST;
		}
		
		if(!user.isEnabled())
		{
			if( user.getLoginAttemptsLeft() == 0 )
			{
				return AccountState.LOCKED;
			}
			else
			{
				return AccountState.DEACTIVATED;
			}
		}

		return AccountState.OK;
	}

	@Override
	public String getEncodedPassword(String email)
	{
		UserDetails user = loadUserByUsername(email);
		return user.getPassword();
	}

	@Override
	public Date getPasswordLastChangeDate(String email)
	{
		AuthenticationUser user = (AuthenticationUser) loadUserByUsername(email);
		Date retVal = user.getPasswordLastChangeDate();
		return retVal;
	}
}
