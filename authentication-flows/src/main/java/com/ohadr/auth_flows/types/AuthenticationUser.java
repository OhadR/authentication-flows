package com.ohadr.auth_flows.types;


import java.util.Date;

public class AuthenticationUser 
{
	private String 		email;
	private boolean 	activated;
	private Date 		passwordLastChangeDate;
	
	

	public String getEmail() 
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public boolean isActivated() 
	{
		return activated;
	}

	public void setActivated(boolean b) 
	{
		activated = b;
	}

	public int getLoginAttemptsCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setLoginAttemptsCounter(int attempts) {
		// TODO Auto-generated method stub
		
	}

	public void setPassword(String newPassword) {
		// TODO Auto-generated method stub
		
	}

	public void setPasswordLastChangeDate(Date date) 
	{
		passwordLastChangeDate = date;
	}

	public Date getPasswordLastChangeDate() 
	{
		return passwordLastChangeDate;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

}
