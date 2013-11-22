package com.ohadr.oauth_srv.core;

import org.apache.commons.lang3.tuple.Pair;

import com.ohadr.oauth_srv.interfaces.OAuthDataManagement;
import com.ohadr.oauth_srv.types.AuthenticationPolicy;
import com.ohadr.oauth_srv.types.OauthAccountState;

public class OAuthDataMngmntImpl implements OAuthDataManagement 
{

	@Override
	public boolean setLoginSuccessForUser(String username) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Pair<String, String> createAccount(String email,
			String encodedPassword, String secretQuestion, String encodedAnswer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticationPolicy getAuthenticationSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OauthAccountState isAccountLocked(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendPasswordRestoreMail(String email, String redirectUri) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSecretAnswer(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setLoginFailureForUser(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendUnlockAccountMail(String email, String redirectUri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String email, String encodedPassword) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pair<String, String> changePassword(String email,
			String encodedCurrentPassword, String encodedNewPassword) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSecretQuestion(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
