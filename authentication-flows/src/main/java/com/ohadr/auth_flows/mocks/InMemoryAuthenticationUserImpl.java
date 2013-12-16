package com.ohadr.auth_flows.mocks;


import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

import com.ohadr.auth_flows.interfaces.AuthenticationUser;

public class InMemoryAuthenticationUserImpl implements AuthenticationUser 
{
	private String 		email;
	private String 		password;
	private boolean 	activated;
	private Date 		passwordLastChangeDate;
	private int 		loginAttemptsLeft;

	
	public InMemoryAuthenticationUserImpl(
			String username,
			String password,
			boolean activated,
			int	loginAttemptsLeft,
			Date passwordLastChangeDate)
	{
		this.email = username;
		this.password = password;
		this.activated = activated;
		this.loginAttemptsLeft = loginAttemptsLeft;
		this.passwordLastChangeDate = passwordLastChangeDate;
	}

	@Override
	public String getUsername() 
	{
		return email;
	}


	@Override
	public String getPassword()
	{
		return password;
	}
	
	@Override
	public boolean isEnabled() 
	{
		return activated;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getLoginAttemptsLeft()
	 */
	@Override
	public int getLoginAttemptsLeft() 
	{
		return loginAttemptsLeft;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getPasswordLastChangeDate()
	 */
	@Override
	public Date getPasswordLastChangeDate() 
	{
		return passwordLastChangeDate;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAccountNonLocked() 
	{
		return !isAccountLocked();
	}

	/**
	 * account is "locked" if it is disabled, and the num attempts exceeds the max-Attempts.
	 * @return
	 */
	public boolean isAccountLocked() 
	{
		return !isEnabled() && (loginAttemptsLeft <= 0);
	}

	@Override
	public boolean isCredentialsNonExpired() 
	{
		//TODO calc the passwordLastChangeDate with the time from policy (account-axpiry)
//		passwordLastChangeDate;
		return true;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}


}
