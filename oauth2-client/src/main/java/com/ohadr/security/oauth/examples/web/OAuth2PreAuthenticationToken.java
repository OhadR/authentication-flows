package com.ohadr.security.oauth.examples.web;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class OAuth2PreAuthenticationToken extends AbstractAuthenticationToken
{
	private static final long serialVersionUID = 1L;
	private final String principal;

	public OAuth2PreAuthenticationToken(String principal)
	{
		super(AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
		this.principal = principal;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities()
	{
		return super.getAuthorities();
	}

	@Override
	public Object getCredentials()
	{
		return null;
	}

	@Override
	public Object getPrincipal()
	{
		return principal;
	}
}
