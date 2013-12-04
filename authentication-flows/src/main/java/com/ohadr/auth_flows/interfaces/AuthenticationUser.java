package com.ohadr.auth_flows.interfaces;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationUser extends UserDetails
{


	public int getLoginAttemptsCounter();

	public void setLoginAttemptsCounter(int attempts);

	public void setPasswordLastChangeDate(Date date);

	public Date getPasswordLastChangeDate();
}