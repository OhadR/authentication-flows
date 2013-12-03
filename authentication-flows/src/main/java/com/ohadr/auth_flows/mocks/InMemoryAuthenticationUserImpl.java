package com.ohadr.auth_flows.mocks;


import java.util.Date;

import com.ohadr.auth_flows.interfaces.AuthenticationUser;

public class InMemoryAuthenticationUserImpl implements AuthenticationUser 
{
	private String 		email;
	private String 		password;
	private boolean 	activated;
	private Date 		passwordLastChangeDate;
	private int 		loginAttemptsCounter;
	
	

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#getEmail()
	 */
	@Override
	public String getEmail() 
	{
		return email;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#setEmail(java.lang.String)
	 */
	@Override
	public void setEmail(String email)
	{
		this.email = email;
	}


	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#setPassword(java.lang.String)
	 */
	@Override
	public void setPassword(String newPassword) 
	{
		password = newPassword;
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
	public boolean isActivated() 
	{
		return activated;
	}

	/* (non-Javadoc)
	 * @see com.ohadr.auth_flows.interfaces.AuthenticationUser#setActivated(boolean)
	 */
	@Override
	public void setActivated(boolean b) 
	{
		activated = b;
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


}
