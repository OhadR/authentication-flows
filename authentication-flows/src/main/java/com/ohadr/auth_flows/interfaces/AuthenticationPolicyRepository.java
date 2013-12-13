package com.ohadr.auth_flows.interfaces;

import com.ohadr.auth_flows.types.AuthenticationPolicy;

public interface AuthenticationPolicyRepository
{
	AuthenticationPolicy getDefaultAuthenticationPolicy();

	AuthenticationPolicy getAuthenticationPolicy(int settingsId);
}
