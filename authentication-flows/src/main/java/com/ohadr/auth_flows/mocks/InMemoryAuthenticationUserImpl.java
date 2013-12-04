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
	private int 		loginAttemptsCounter;
	
	
	public InMemoryAuthenticationUserImpl(
			String username,
			String password,
			boolean activated)
	{
		this.email = username;
		this.password = password;
		this.activated = activated;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getEmail()
	 */
	@Override
	public String getUsername() 
	{
		return email;
	}


	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getPassword()
	 */
	@Override
	public String getPassword()
	{
		return password;
	}
	
	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#isActivated()
	 */
	@Override
	public boolean isEnabled() 
	{
		return activated;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getLoginAttemptsCounter()
	 */
	@Override
	public int getLoginAttemptsCounter() 
	{
		return loginAttemptsCounter;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#setLoginAttemptsCounter(int)
	 */
	@Override
	public void setLoginAttemptsCounter(int attempts) 
	{
		loginAttemptsCounter = attempts;
	}


	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#setPasswordLastChangeDate(java.util.Date)
	 */
	@Override
	public void setPasswordLastChangeDate(Date date) 
	{
		passwordLastChangeDate = date;
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
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}


}
