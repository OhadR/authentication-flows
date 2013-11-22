package com.ohadr.oauth_srv.core;


import java.util.Date;

import org.springframework.stereotype.Repository;

import com.ohadr.oauth_srv.interfaces.OAuthRepository;
import com.ohadr.oauth_srv.types.OAuthUser;
import com.ohadr.oauth_srv.types.OauthAccountState;

@Repository
public abstract class OAuthRepositoryImpl implements OAuthRepository
{
	public OAuthRepositoryImpl()
	{
		System.out.println(this.getClass().getName() + " created");
	}
	

	@Override
	public void setEnabled (String email)
	{
		User user = getUser(email);
		user.setEnabled(true);
	}
	

	@Override
	public boolean setLoginFailure(String email, int maxPasswordEntryAttempts) 
	{
		OAuthUser user = getUser(email);
		
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
		OAuthUser user = getUser(email);
		
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
	public OauthAccountState isAccountLocked(String email) 
	{
		User user = getUser(email);
		
		//user does not exist:
		if(user == null)
		{
			return OauthAccountState.NOT_EXIST;
		}
		
		boolean isLocked = !user.getEnabled() && user.getLoginAttemptsCounter() != 0;
		if(isLocked)
		{
			return OauthAccountState.LOCKED;
		}
		return OauthAccountState.OK;
	}

	@Override
	public boolean setPassword(String email, String newPassword)
	{
		OAuthUser user = getUser(email);
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
	public String getEncodedSecretAnswer(String email)
	{
		OAuthUser user = getUser(email);
		return user.getAnswerToSecretQuestion();
	}

	@Override
	public String getEncodedPassword(String email)
	{
		OAuthUser user = getUser(email);
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
		OAuthUser user = getUser(email);
		Date retVal = null;
		if(user != null)
		{
			retVal = user.getPasswordLastChangeDate();
		}
		return retVal;
	}
}
