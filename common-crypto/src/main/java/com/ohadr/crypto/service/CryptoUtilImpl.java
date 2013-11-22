package com.ohadr.crypto.service;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ohadr.crypto.exception.CryptoException;
import com.watchdox.security.crypto.CryptoProvider;

@Component("cryptoUtil")
@Scope("singleton")
public class CryptoUtilImpl implements ICryptoUtil, InitializingBean
{
	private CryptoProvider activeProvider;

	/**
	 * this const is used both by the oAuth server and the webapp. when the webapp wanna send hashed value to
	 * the oAuth srv (or vice versa), they use this constant
	 */
	public static final String WATCHDOX_HASH_VALUE = "watchdox-hash-123";

	/**
	 * The size of a bare DSA signature (two 160-bit integers)
	 */
	static public final int BARE_DSA_SIGNATURE_SIZE = 40;


	
	public void afterPropertiesSet() throws Exception
	{
		// Check preconditions: Maximum key length
		try
		{
			if (Cipher.getMaxAllowedKeyLength("AES") < Integer.MAX_VALUE)
			{
				String tutorialUrl = "http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml";
				throw new CryptoException(
				    "Your system has a restriction on the encryption algorithm key length. Please remove this restriction. For more info, see "
				        + tutorialUrl);
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new CryptoException("No AES provider is installed on your system ?!", e);
		}

	}

	public Key getCryptoKey(String seed)
	{
		return activeProvider.getSeededKey(new ImmutablePair("hive", seed));
	}




	private byte[] encryptBytes(byte[] data, Key key)
	{
		try
		{
			Cipher cipher = activeProvider.getCipher(key, Cipher.ENCRYPT_MODE);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e)
		{
			throw new CryptoException("Crypto engine failed to encrypt", e);
		}
	}

	private byte[] decryptBytes(byte[] data, Key key) throws IllegalBlockSizeException, BadPaddingException
	{
		try
		{
			Cipher cipher = activeProvider.getCipher(key, Cipher.DECRYPT_MODE);
			return cipher.doFinal(data);
		}
		catch (InvalidKeyException e)
		{
			throw new CryptoException("Crypto engine failed to initialize decryption", e);
		}
	}

	//@Override
	public String encryptAndBase64(byte[] data, Key key)
	{
		return Base64.encodeBase64String(encryptBytes(data, key));
	}

	//@Override
	public byte[] decryptBase64(String base64andEncrypted, Key key) throws IllegalBlockSizeException,
	                                                               BadPaddingException
	{
		return decryptBytes(Base64.decodeBase64(base64andEncrypted), key);
	}

}
