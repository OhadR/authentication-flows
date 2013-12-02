package com.ohadr.auth_flows.interfaces;

import java.util.Date;

public interface AuthenticationUser 
{
	String getEmail();
	void setEmail(String email);

	public boolean isActivated();

	public void setActivated(boolean b);

	public int getLoginAttemptsCounter();

	public void setLoginAttemptsCounter(int attempts);

	public void setPassword(String newPassword);

	public void setPasswordLastChangeDate(Date date);

	public Date getPasswordLastChangeDate();

	public String getPassword();
}