package com.ohadr.crypto.service;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.ohadr.crypto.config.CryptoProperties;



@Controller
public class PublicKeyController
{
	@Autowired
	private CryptoProperties cryptoProperties;

//	private PrivateKey privateKey;
	private PublicKey publicKey;

	
	public String getPublicKey() throws Exception
	{
		try
		{
			//note: we depend on the DefaultCryptoProvider !! names must match here and there!
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(cryptoProperties.getSimpleKeystore()), cryptoProperties.getSimplePassword().toCharArray());
//			char[] keyPassword = (cryptoProperties.getSimplePassword() + "__" + cryptoProperties.getKeyAlias()).toCharArray();	
//			privateKey = (PrivateKey) ks.getKey(cryptoProperties.getKeyAlias(), keyPassword);
			publicKey = ks.getCertificate(cryptoProperties.getKeyAlias()).getPublicKey();
		}
		catch (Throwable e)
		{
			throw new IllegalArgumentException("Failed to read the private token key from the keystore", e);
		}

		return Base64.encodeBase64String( publicKey.getEncoded() );
	}

}
