package com.ohadr.oauth.resource_server.token;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

public class MyTokenStore implements TokenStore 
{

	public MyTokenStore() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token)
	{
        //dummy
		DefaultAuthorizationRequest authorizationRequest = new DefaultAuthorizationRequest(new HashMap<String, String>());
		authorizationRequest.setApproved(true);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(authorizationRequest, null);
        return oAuth2Authentication;
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token,
			OAuth2Authentication authentication) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken) 
	{
		DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken) ;
		String data = accessToken.split(":")[0];
        Map<String, String> fields = parseQueryString(data);
        String user = fields.get("user");
        String expiresStr = fields.get("expires");
        String issuer = fields.get("issuer");
        
        Long expiresSec = Long.valueOf(expiresStr);
        Date expiration = new Date(expiresSec * 1000);	//Date accepts milisecs
		token.setExpiration(expiration);

		return token;
	}
	
    private Map<String, String> parseQueryString(String queryString)
    {
        if (queryString == null)
        {
            throw new IllegalArgumentException();
        }

        Map<String, String> params = new HashMap<String, String>();
        for (String param : queryString.split("&"))
        {
            String[] pair = param.split("=");
            if (pair.length == 2)
            {
                try
                {
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = URLDecoder.decode(pair[1], "UTF-8");
                    params.put(key, value);
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return params;
    }


	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken,
			OAuth2Authentication authentication) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(
			OAuth2RefreshToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAccessTokenUsingRefreshToken(
			OAuth2RefreshToken refreshToken) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

}
