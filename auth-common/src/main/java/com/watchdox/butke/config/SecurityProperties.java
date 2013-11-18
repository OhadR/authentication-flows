package com.watchdox.butke.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityProperties
{
	@Value("${com.watchdox.oauth2.sign.keyStore}")
	private String keystore;

	@Value("${com.watchdox.oauth2.sign.storePass}")
	private String storepass;

	@Value("${com.watchdox.oauth2.sign.keyAlias}")
	private String keysToken;

	public String getKeystore()
	{
		return keystore;
	}

	public void setKeystore(String keystore)
	{
		this.keystore = keystore;
	}

	public String getStorepass()
	{
		return storepass;
	}

	public void setStorepass(String storepass)
	{
		this.storepass = storepass;
	}

	public String getKeysToken()
	{
		return keysToken;
	}

	public void setKeysToken(String keysToken)
	{
		this.keysToken = keysToken;
	}

}
