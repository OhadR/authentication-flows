package com.ohadr.auth_flows.core;


import java.util.Date;

import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.types.AccountState;
import com.ohadr.auth_flows.types.AuthenticationUser;


public abstract class AbstractAuthenticationAccountRepository implements AuthenticationAccountRepository
{
	public AbstractAuthenticationAccountRepository()
	{
		System.out.println(this.getClass().getName() + " created");
	}
	

	@Override
	public void setEnabled (String email)
	{
		AuthenticationUser user = getUser(email);
		user.setEnabled(true);
	}
	

	@Override
	public boolean setLoginFailure(String email, int maxPasswordEntryAttempts) 
	{
		AuthenticationUser user = getUser(email);
		
		//user does not exist:
		if(user == null)
		{
			return false;
		}
		
		int attempts = user.getLoginAttemptsCounter();
		if(++attempts >= maxPasswordEntryAttempts)
		{
			//lock the user:
			user.setEnabled(false);
			return true;
		}
		else
		{
			user.setLoginAttemptsCounter(attempts);
			return false;
		}
	}

	@Override
	public void setLoginSuccess(String email) 
	{
		AuthenticationUser user = getUser(email);
		
		//user might be null since we "login-success" once to the user account, and then to the client-application (oAuth mechanism)
		//so if 'email' is the "client app", there will be no 'user' and it will be null:
		if(user != null)
		{
			user.setLoginAttemptsCounter( 0 );
		}	
	}

	/**
	 * account is locked only if the enable is false AND attempts cntr is NOT 0. o/w, if counter is 0, we deal with 
	 * not-activated account.
	 */
	@Override
	public AccountState isAccountLocked(String email) 
	{
		AuthenticationUser user = getUser(email);
		
		//user does not exist:
		if(user == null)
		{
			return AccountState.NOT_EXIST;
		}
		
		boolean isLocked = !user.getEnabled() && user.getLoginAttemptsCounter() != 0;
		if(isLocked)
		{
			return AccountState.LOCKED;
		}
		return AccountState.OK;
	}

	@Override
	public boolean setPassword(String email, String newPassword)
	{
		AuthenticationUser user = getUser(email);
		if(user != null)
		{
			user.setPassword(newPassword);
			user.setPasswordLastChangeDate(new Date( System.currentTimeMillis() ));
			return true;
		}
		else
		{
			return false;
		}
	}


	@Override
	public String getEncodedPassword(String email)
	{
		AuthenticationUser user = getUser(email);
		String retVal = null;
		if(user != null)
		{
			retVal = user.getPassword();
		}
		return retVal;
	}

	@Override
	public Date getPasswordLastChangeDate(String email)
	{
		AuthenticationUser user = getUser(email);
		Date retVal = null;
		if(user != null)
		{
			retVal = user.getPasswordLastChangeDate();
		}
		return retVal;
	}
}
