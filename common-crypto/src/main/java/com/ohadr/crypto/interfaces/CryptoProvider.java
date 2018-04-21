package com.ohadr.crypto.interfaces;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.Cipher;

public interface CryptoProvider
{
	/**
	 * Returns a seeded key based on the given key parameters. Given the same key parameters, the method will always
	 * return the same key.
	 * 
	 * @param keyParam
	 *            Specifies the hive and the seed for generating the seeded key.
	 * @return A key that can be used with the getCipher() method.
	 */
	Key getKey(KeyHive keyParam);

	/**
	 * Returns a cipher object for symmetric data encrpytion.
	 * 
	 * @param key
	 *            The encryption key returned from getSeededKey(). Other keys may not be accepted.
	 * @param cryptMode
	 *            One of Cipher.ENCRYPT_MODE, Cipher.DECRYPT_MODE.
	 * @return A Cipher object that can be used for encrpytion/description, depending on the value of cryptMode.
	 */
	Cipher getCipher(Key key, int cryptMode) throws InvalidKeyException;

	/**
	 * Returns the server's private key used for digitally signing data.
	 * /
	PrivateKey loadPrivateKey();

	/**
	 * Returns the server's certificate used for digitally validating signatures.
	 * /
	Certificate loadCertificate();

	/**
	 * Returns the signature algorithm (RSA/DSA)
	 * /
	Signature getSignatureAlgorithm();*/
}
