package com.ohadr.crypto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class CryptoProperties
{
	@Value("${com.ohadr.crypto.keystore}")
	private String simpleKeystore;

	@Value("${com.ohadr.crypto.password}")
	private String simplePassword;

	@Value("${com.ohadr.crypto.keyAlias}")
	private String keyAlias;

	public String getSimpleKeystore()
	{
		return simpleKeystore;
	}

	public void setSimpleKeystore(String simpleKeystore)
	{
		this.simpleKeystore = simpleKeystore;
	}

	public String getSimplePassword()
	{
		return simplePassword;
	}

	public void setSimplePassword(String simplePassword)
	{
		this.simplePassword = simplePassword;
	}

	public String getKeyAlias() 
	{
		return keyAlias;
	}
}
