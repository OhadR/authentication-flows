package com.ohadr.crypto.service;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ohadr.crypto.config.CryptoProperties;
import com.ohadr.crypto.interfaces.KeystoreService;


@Service("keystoreService")
public class KeystoreServiceImpl implements InitializingBean, KeystoreService
{
	@Autowired
	private CryptoProperties cryptoProperties;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public void afterPropertiesSet() throws Exception
	{
		try
		{
			//note: we depend on the DefaultCryptoProvider !! names must match here and there!
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(cryptoProperties.getSimpleKeystore()), cryptoProperties.getSimplePassword().toCharArray());
			
//			char[] keyPassword = (cryptoProperties.getSimplePassword() + "__" + cryptoProperties.getKeyAlias()).toCharArray();	
//			privateKey = (PrivateKey) ks.getKey(cryptoProperties.getKeyAlias(), keyPassword);
			privateKey = (PrivateKey) ks.getKey(cryptoProperties.getKeyAlias(), cryptoProperties.getSimplePassword().toCharArray());
			
			publicKey = ks.getCertificate(cryptoProperties.getKeyAlias()).getPublicKey();
		}
		catch (Throwable e)
		{
			throw new IllegalArgumentException("Failed to read the private token key from the keystore", e);
		}
	}

	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

	public PublicKey getPublicKey()
	{
		return publicKey;
	}
}
