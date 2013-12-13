package com.ohadr.auth_flows.types;

import java.util.*;

import org.springframework.util.CollectionUtils;

public class AuthenticationPolicy
{
	private static final String DB_ITEMS_DELIMITER = ";";

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



	@SuppressWarnings("unchecked")
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
		this.maxPasswordEntryAttempts = maxPasswordEntryAttempts;
		this.passwordLifeInDays = passwordLifeInDays;
		this.rememberMeTokenValidityInDays = rememberMeTokenValidityInDays;
		
		this.passwordBlackList = CollectionUtils.arrayToList( passwordBlackList.split(DB_ITEMS_DELIMITER)); 

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
