package com.ohadr.authentication.token;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ohadr.authentication.config.SecurityProperties;


@Service("keystoreService")
public class KeystoreServiceImpl implements InitializingBean, KeystoreService
{
	@Autowired
	private SecurityProperties securityProperties;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		try
		{
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(securityProperties.getKeystore()), securityProperties.getStorepass()
			                                                                                 .toCharArray());
			privateKey = (PrivateKey) ks.getKey(securityProperties.getKeysToken(), securityProperties.getStorepass()
			                                                                                         .toCharArray());
			publicKey = ks.getCertificate(securityProperties.getKeysToken()).getPublicKey();
		}
		catch (Throwable e)
		{
			throw new IllegalArgumentException("Failed to read the private token key from the keystore", e);
		}
	}

	@Override
	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

	@Override
	public PublicKey getPublicKey()
	{
		return publicKey;
	}

	public void setSecurityProperties(SecurityProperties securityProperties)
	{
		this.securityProperties = securityProperties;
	}
}
