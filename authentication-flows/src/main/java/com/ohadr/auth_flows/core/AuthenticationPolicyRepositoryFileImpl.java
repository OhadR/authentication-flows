package com.ohadr.auth_flows.core;

import org.springframework.beans.factory.annotation.Value;

import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.types.AuthenticationPolicy;

public class AuthenticationPolicyRepositoryFileImpl implements
		AuthenticationPolicyRepository
{
	@Value("${com.ohadr.auth-flows.policy.passwordLifeInDays}")
	private int passwordLifeInDays;
	
	@Value("${com.ohadr.auth-flows.policy.passwordMinLength}")
	private int passwordMinLength;
	
	@Value("${com.ohadr.auth-flows.policy.passwordMaxLength}")
	private int passwordMaxLength;
	
	@Value("${com.ohadr.auth-flows.policy.passwordMinUpCaseChars}")
	private int passwordMinUpCaseChars;
	
	@Value("${com.ohadr.auth-flows.policy.passwordMinLoCaseChars}")
	private int passwordMinLoCaseChars;

	@Value("${com.ohadr.auth-flows.policy.passwordMinDigits}")
	private int passwordMinDigits;

	@Value("${com.ohadr.auth-flows.policy.passwordMinSpecialSymbols}")
	private int passwordMinSpecialSymbols;

	@Value("${com.ohadr.auth-flows.policy.passwordBlackList}")
	private String passwordBlackList;

	@Value("${com.ohadr.auth-flows.policy.rememberMeTokenValidityInDays}")
	private int rememberMeTokenValidityInDays;

	@Value("${com.ohadr.auth-flows.policy.passwordMaxEntryAttempts}")
	private int passwordMaxEntryAttempts;

	@Override
	public AuthenticationPolicy getDefaultAuthenticationPolicy()
	{
		int settings = 1;
		return getAuthenticationPolicy( settings );
	}

	@Override
	public AuthenticationPolicy getAuthenticationPolicy(int settingsId)
	{
		AuthenticationPolicy policy = new AuthenticationPolicy(
				passwordMinLength,
				passwordMaxLength,
				passwordMinUpCaseChars,
				passwordMinLoCaseChars,
				passwordMinDigits,
				passwordMinSpecialSymbols,
				passwordBlackList,
				passwordMaxEntryAttempts,
				passwordLifeInDays,
				rememberMeTokenValidityInDays
					);

			return policy;
	}

}
