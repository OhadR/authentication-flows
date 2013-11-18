package com.watchdox.security.oauth.examples.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * User: shalom
 */
public class WatchdoxExampleResourceServerTokenServices implements ResourceServerTokenServices
{
    private TokenStore tokenStore;
    private String supportRefreshToken;
    private UserDetailsService userDetailsService;

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException
    {
        //TODO:check token expiration

        String data = accessToken.split(":")[0];
        Map<String, String> fields = parseQueryString(data);
        String user = fields.get("user");
        String expires = fields.get("expires");
        String issuer = fields.get("issuer");

        UserDetails userDetails = userDetailsService.loadUserByUsername(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(),userDetails.getPassword(),userDetails.getAuthorities());


        //dummy
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(new HashMap<String, String>()).approved(true);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(authorizationRequest,authenticationToken);
        return oAuth2Authentication;
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

    public void setTokenStore(TokenStore tokenStore)
    {
        this.tokenStore = tokenStore;
    }

    public TokenStore getTokenStore()
    {
        return tokenStore;
    }

    public void setSupportRefreshToken(String supportRefreshToken)
    {
        this.supportRefreshToken = supportRefreshToken;
    }

    public String getSupportRefreshToken()
    {
        return supportRefreshToken;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

    public UserDetailsService getUserDetailsService()
    {
        return userDetailsService;
    }
}
