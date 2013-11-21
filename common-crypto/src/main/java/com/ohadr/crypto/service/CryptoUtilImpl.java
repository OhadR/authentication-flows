package com.ohadr.crypto.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ohadr.crypto.exception.CryptoException;
import com.watchdox.security.crypto.CryptoProvider;
import com.watchdox.security.crypto.KeyHive;
import com.watchdox.security.crypto.KeyParameters;

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


	
	//@Override
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

	//@Override
	public Key getCryptoKey(KeyHive hive, String seed)
	{
		if (hive == KeyHive.SEALED_256)
		{
			return activeProvider.getExtractable256BitKey(seed);
		}
		else
		{
			return activeProvider.getSeededKey(new KeyParameters(hive, seed));
		}
	}

	private Certificate getCertificate()
	{
		return activeProvider.loadCertificate();
	}

	//@Override
	public InputStream decrpytStream(InputStream sourceStream, Key key)
	{
		try
		{
			return new CipherInputStream(sourceStream, activeProvider.getCipher(key, Cipher.DECRYPT_MODE));
		}
		catch (InvalidKeyException e)
		{
			throw new CryptoException("Crypto engine failed to initialize decryption", e);
		}
	}

	//@Override
	public InputStream encryptStream(InputStream sourceStream, Key key)
	{
		try
		{
			return new CipherInputStream(sourceStream, activeProvider.getCipher(key, Cipher.ENCRYPT_MODE));
		}
		catch (InvalidKeyException e)
		{
			throw new CryptoException("Crypto engine failed to initialize encryption", e);
		}
	}

	//@Override
	public OutputStream decryptStream(OutputStream sourceStream, Key key)
	{
		try
		{
			return new CipherOutputStream(sourceStream, activeProvider.getCipher(key, Cipher.DECRYPT_MODE));
		}
		catch (InvalidKeyException e)
		{
			throw new CryptoException("Crypto engine failed to initialize decryption", e);
		}
	}

	//@Override
	public OutputStream encryptStream(OutputStream sourceStream, Key key)
	{
		try
		{
			return new CipherOutputStream(sourceStream, activeProvider.getCipher(key, Cipher.ENCRYPT_MODE));
		}
		catch (InvalidKeyException e)
		{
			throw new CryptoException("Crypto engine failed to initialize encryption", e);
		}
	}

	//@Override
	public byte[] encryptBytes(byte[] data, Key key)
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

	//@Override
	public byte[] decryptBytes(byte[] data, Key key) throws IllegalBlockSizeException, BadPaddingException
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

	//@Override
	public byte[] getSealerKey(String documentUuid)
	{
		return activeProvider.getExtractable256BitKey("Sealer/" + documentUuid).getEncoded();
	}

	//@Override
	public byte[] signApiToken(byte[] data)
	{
		PrivateKey privateKey = activeProvider.loadPrivateKey();
		Signature signature = activeProvider.getSignatureAlgorithm();
		try
		{
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		}
		catch (GeneralSecurityException e)
		{
			throw new CryptoException("Crypto engine failed to sign", e);
		}
	}

	//@Override
	public boolean validateApiTokenSignature(PublicKey publicKey, byte[] data, byte[] signatureData)
	{
		Signature signature = activeProvider.getSignatureAlgorithm();
		try
		{
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(signatureData);
		}
		catch (SignatureException e)
		{
			return false;
		}
		catch (GeneralSecurityException e)
		{
			throw new CryptoException("Crypto engine failed to validate signature", e);
		}
	}

	//@Override
	public boolean validateApiTokenSignature(byte[] data, byte[] signatureData)
	{
		PublicKey publicKey = getCertificate().getPublicKey();
		return validateApiTokenSignature(publicKey, data, signatureData);
	}
	
	
	

   // @Override
    public char[] encryptBytesToHex(byte[] data, Key cryptoKey) {
        return Hex.encodeHex(encryptBytes(data, cryptoKey));
    }

    //@Override
    public byte[] decryptBytesFromHex(char[] data, Key cryptoKey) throws IllegalBlockSizeException, BadPaddingException, DecoderException {
        return decryptBytes(Hex.decodeHex(data),cryptoKey);
    }
    
    
	public boolean validateSignature(PublicKey key, String signatureStr, String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
	{
			byte[] decodedSignature = Base64.decodeBase64(signatureStr);

			if (decodedSignature.length == CryptoUtilImpl.BARE_DSA_SIGNATURE_SIZE)
			{
				decodedSignature = convertDsaSignatureToDerEncoding(decodedSignature);
			}

			final String SIGNATURE_ALGORITHM = "DSA";
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(key);
			signature.update(data.getBytes());
			if (!signature.verify(decodedSignature))
			{
				return false;
			}
		
		return true;
	}

	public byte[] convertDsaSignatureToDerEncoding(byte[] dsaSignature) {
		// TODO Auto-generated method stub
		return null;
	}

}
