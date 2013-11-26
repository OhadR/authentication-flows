package com.ohadr.auth_flows.types;

import java.util.*;

public class AuthenticationPolicy
{
	private int passwordMinSpecialSymbols;

	private List<String> passwordBlackList;

	private int passwordLifeInDays;

	private int passwordMinLength;

	private int passwordMinLoCaseLetters;

	private int rememberMeTokenValidityInDays;

	private int passwordMinUpCaseLetters;

	private int passwordMaxLength;

	private int passwordMinNumbers;

	private int maxPasswordEntryAttempts;

	/**
	 * List of secret questions for user authentication
	 */
	private List<String> secretQuestions;

	public int getPasswordMinSpecialSymbols()
	{
		return passwordMinSpecialSymbols;
	}

	public void setPasswordMinSpecialSymbols(int passwordMinSpecialSymbols)
	{
		this.passwordMinSpecialSymbols = passwordMinSpecialSymbols;
	}

	public List<String> getPasswordBlackList()
	{
		return passwordBlackList;
	}

	public void setPasswordBlackList(List<String> passwordBlackList)
	{
		this.passwordBlackList = passwordBlackList;
	}

	public int getPasswordLifeInDays()
	{
		return passwordLifeInDays;
	}

	public void setPasswordLifeInDays(int passwordLifeInDays)
	{
		this.passwordLifeInDays = passwordLifeInDays;
	}

	public int getPasswordMinLength()
	{
		return passwordMinLength;
	}

	public void setPasswordMinLength(int passwordMinLength)
	{
		this.passwordMinLength = passwordMinLength;
	}

	public int getPasswordMinLoCaseLetters()
	{
		return passwordMinLoCaseLetters;
	}

	public void setPasswordMinLoCaseLetters(int passwordMinLoCaseLetters)
	{
		this.passwordMinLoCaseLetters = passwordMinLoCaseLetters;
	}

	public int getRememberMeTokenValidityInDays()
	{
		return rememberMeTokenValidityInDays;
	}

	public void setRememberMeTokenValidityInDays(int rememberMeTokenValidityInDays)
	{
		this.rememberMeTokenValidityInDays = rememberMeTokenValidityInDays;
	}

	public int getPasswordMinUpCaseLetters()
	{
		return passwordMinUpCaseLetters;
	}

	public void setPasswordMinUpCaseLetters(int passwordMinUpCaseLetters)
	{
		this.passwordMinUpCaseLetters = passwordMinUpCaseLetters;
	}

	public int getPasswordMaxLength()
	{
		return passwordMaxLength;
	}

	public void setPasswordMaxLength(int passwordMaxLength)
	{
		this.passwordMaxLength = passwordMaxLength;
	}

	public int getPasswordMinNumbers()
	{
		return passwordMinNumbers;
	}

	public void setPasswordMinNumbers(int passwordMinNumbers)
	{
		this.passwordMinNumbers = passwordMinNumbers;
	}

	public int getMaxPasswordEntryAttempts()
	{
		return maxPasswordEntryAttempts;
	}

	public void setMaxPasswordEntryAttempts(int maxPasswordEntryAttempts)
	{
		this.maxPasswordEntryAttempts = maxPasswordEntryAttempts;
	}

	public List<String> getSecretQuestions()
	{
		return secretQuestions;
	}

	public void setSecretQuestions(List<String> secretQuestions)
	{
		this.secretQuestions = secretQuestions;
	}

}
