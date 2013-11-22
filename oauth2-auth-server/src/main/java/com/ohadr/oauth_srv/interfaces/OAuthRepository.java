package com.ohadr.oauth_srv.interfaces;

import com.ohadr.oauth_srv.types.OAuthUser;

public interface OAuthRepository 
{

	OAuthUser getUser(String email);

	void deleteOAuthUser(String email);

	void createUser(String email, 
			String encodedPassword,
			String secretQuestion, 
			String encodedAnswer);

	String getEncodedPassword(String username);

	boolean changePassword(String username, String newEncodedPassword);

}
