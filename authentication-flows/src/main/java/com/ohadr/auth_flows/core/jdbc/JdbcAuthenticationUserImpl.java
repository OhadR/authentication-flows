package com.ohadr.auth_flows.core.jdbc;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ohadr.auth_flows.interfaces.AuthenticationUser;

/**
 * The persistent class for the users database table.
 */
@Entity
@Table(name = "users")
public class JdbcAuthenticationUserImpl implements Serializable, AuthenticationUser
{
	private static final long serialVersionUID = 1L;

	@Id
	private String username;

	private boolean activated;

	private String password;
	
	@Column (name = "login_attempts_counter")
	private int loginAttemptsCounter;

	@Column(name = "LAST_PSWD_CHANGE_DATE")
	private Date passwordLastChangeDate;

	
	public JdbcAuthenticationUserImpl()
	{
	}


	@Override
	public String getEmail() 
	{
		return this.username;
	}

	@Override
	public void setEmail(String email) 
	{
		this.username = email;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}


	public int getLoginAttemptsCounter() 
	{
		return loginAttemptsCounter;
	}

	public void setLoginAttemptsCounter(int loginAttemptsCounter) 
	{
		this.loginAttemptsCounter = loginAttemptsCounter;
	}

	public Date getPasswordLastChangeDate() 
	{
		return passwordLastChangeDate;
	}

	public void setPasswordLastChangeDate(Date passwordLastChangeDate) 
	{
		this.passwordLastChangeDate = passwordLastChangeDate;
	}

	@Override
	public boolean isActivated() 
	{
		return activated;
	}

	@Override
	public void setActivated(boolean b) 
	{
		activated = b;
	}

}