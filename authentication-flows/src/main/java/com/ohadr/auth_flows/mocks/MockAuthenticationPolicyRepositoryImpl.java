package com.ohadr.auth_flows.mocks;

import com.ohadr.auth_flows.interfaces.AuthenticationPolicyRepository;
import com.ohadr.auth_flows.types.AuthenticationPolicy;

public class MockAuthenticationPolicyRepositoryImpl implements
		AuthenticationPolicyRepository
{
	private static final int MaxPasswordEntryAttempts = 5;
	private static final int setPasswordMaxLength = 8;
	private static final int setRememberMeTokenValidityInDays = 30;

	@Override
	public AuthenticationPolicy getDefaultAuthenticationPolicy() 
	{
		return getAuthenticationPolicy( 0 );
	}

	@Override
	public AuthenticationPolicy getAuthenticationPolicy(int settingsId)
	{
		AuthenticationPolicy policy = new AuthenticationPolicy(
				MaxPasswordEntryAttempts,
				setPasswordMaxLength,
				setRememberMeTokenValidityInDays, 0, 0,
				0,
				"string",
				0,0,0);

		return policy;
	}
}
