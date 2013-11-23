package com.ohadr.oauth_srv.mocks;

import com.ohadr.oauth_srv.core.AbstractOAuthRepository;
import com.ohadr.oauth_srv.types.OAuthUser;


public class OAuthRepositoryMock extends AbstractOAuthRepository 
{

	@Override
	public void createAccount(String email, String encodedPassword)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public OAuthUser getUser(String email) 
	{
		OAuthUser user = new OAuthUser();
		
		return user;
	}

	@Override
	public void deleteOAuthAccount(String email) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean changePassword(String username, String newEncodedPassword) {
		// TODO Auto-generated method stub
		return false;
	}

}
