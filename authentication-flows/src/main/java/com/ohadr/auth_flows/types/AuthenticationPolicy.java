package com.ohadr.auth_flows.types;

import java.sql.Date;
import java.util.*;

public class AuthenticationPolicy
{
	private int passwordMinLength;

	private int passwordMaxLength;

	private int passwordMinUpCaseChars;

	private int passwordMinLoCaseChars;

	private int passwordMinNumbericDigits;

	private int passwordMinSpecialSymbols;
	
	private List<String> passwordBlackList;
	
	private int maxPasswordEntryAttempts;

	private int passwordLifeInDays;

	private int rememberMeTokenValidityInDays;



	public AuthenticationPolicy(int passwordMinLength,
			int passwordMaxLength,
			int passwordMinUpCaseChars,
			int passwordMinLoCaseChars, 
			int passwordMinNumbericDigits,
			int passwordMinSpecialSymbols,
			String passwordBlackList,
			int maxPasswordEntryAttempts,
			int passwordLifeInDays,
			int rememberMeTokenValidityInDays)
	{
		this.passwordMinLength = passwordMinLength;
		this.passwordMaxLength = passwordMaxLength;
		this.passwordMinUpCaseChars = passwordMinUpCaseChars;
		this.passwordMinLoCaseChars = passwordMinLoCaseChars;
		this.passwordMinNumbericDigits = passwordMinNumbericDigits;
		this.passwordMinSpecialSymbols = passwordMinSpecialSymbols;
//TODO		this.passwordBlackList = passwordBlackList;
		this.maxPasswordEntryAttempts = maxPasswordEntryAttempts;
		this.passwordLifeInDays = passwordLifeInDays;
		this.rememberMeTokenValidityInDays = rememberMeTokenValidityInDays;
	}

	public int getPasswordMinSpecialSymbols()
	{
		return passwordMinSpecialSymbols;
	}

	public List<String> getPasswordBlackList()
	{
		return passwordBlackList;
	}

	public int getPasswordLifeInDays()
	{
		return passwordLifeInDays;
	}

	public int getPasswordMinLength()
	{
		return passwordMinLength;
	}

	public int getPasswordMinLoCaseChars()
	{
		return passwordMinLoCaseChars;
	}

	public int getRememberMeTokenValidityInDays()
	{
		return rememberMeTokenValidityInDays;
	}

	public int getPasswordMinUpCaseChars()
	{
		return passwordMinUpCaseChars;
	}

	public int getPasswordMaxLength()
	{
		return passwordMaxLength;
	}

	public int getPasswordMinNumbericDigits()
	{
		return passwordMinNumbericDigits;
	}

	public int getMaxPasswordEntryAttempts()
	{
		return maxPasswordEntryAttempts;
	}

}
