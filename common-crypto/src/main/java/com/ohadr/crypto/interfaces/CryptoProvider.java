package com.ohadr.crypto.interfaces;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.Cipher;

import org.apache.commons.lang3.tuple.ImmutablePair;

public interface CryptoProvider
{
	/**
	 * Returns an extractable seeded key based on the given key parameters. Given the same key parameters, the method
	 * will always return the same key. The length of the key is always 256 bytes, and the key is extractable in the
	 * sense that calling its getEncoded() method will return an array of 32 bytes.
	 * 
	 * @param seed
	 *            Specifies the seed for the key. Different seeds results in completely different keys.
	 * @return 256-bit extractable secure key based on the given seed.
	 */
//	Key getExtractable256BitKey(String seed);

	/**
	 * Returns a seeded key based on the given key parameters. Given the same key parameters, the method will always
	 * return the same key.
	 * 
	 * @param keyParams
	 *            Specifies the hive and the seed for generating the seeded key.
	 * @return A key that can be used with the getCipher() method.
	 */
	Key getSeededKey(ImmutablePair<?, ?> keyParams);

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
