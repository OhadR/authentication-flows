package com.ohadr.auth_flows.interfaces;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationUser extends UserDetails
{

	/**
	 * the logic: in the DB we count DOWN the attempts of the user. upon account creation and
	 * upon login success, we set the "login attempts left" in the DB as set in the props file.
	 * upon login failure, we decreament the counter. this way, the loginFailureHandler does not 
	 * have to know the "max attempts". only the processor knows this max value. 
	 * @return
	 */
	public int getLoginAttemptsLeft();

	public void setPasswordLastChangeDate(Date date);

	public Date getPasswordLastChangeDate();
}