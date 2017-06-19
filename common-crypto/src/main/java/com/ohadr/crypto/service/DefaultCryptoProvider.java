package com.ohadr.crypto.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.interfaces.CryptoProvider;
import com.ohadr.crypto.interfaces.KeyHive;


/**
 * this class tries to load key-store file, and load the keys from it. if file does not exist, it creates it,
 * creates the keys and stores it. if file exists but one or more keys are missing - it creates the keys and 
 * stores to the file. 
 * 
 * if this jar is deployed where files cannot be stored (GAE, for example), then we have a flag for these cases,
 * so this class will not try to store the key-store file, but will work in-mem.
 * 
 * @author OhadR
 *
 */
public class DefaultCryptoProvider implements CryptoProvider
{
	private static final Logger logger = Logger.getLogger(DefaultCryptoProvider.class);

	private static final String SYMMETRIC_ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final int SYMMETRIC_KEY_LENGTH = 128;		//128 bit (16 bytes)

	private static final String ASYMMETRIC_ALGORITHM = "DSA";
	private static final String ASSYMETRIC_SIGNATURE_ALGORITHM = "SHA256withDSA";
	private static final int ASYMMETRIC_KEY_SIZE = 1024;

	public static final String KEYSTORE_TYPE = "JCEKS";

	private static final String ASYMMETRIC_KEY_NAME = "WatchDox_DSA";

	private static final byte[] ZERO_IV = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	private final KeyStore keyStore;
	private final Map<KeyHive, Key> keys;
	private PrivateKey privateKey;
	private Certificate certificate;

	public DefaultCryptoProvider(
			String keystoreFile, 
			String keystorePassword, 
			boolean createFileIfNotExist)
	{
		try
		{
			keys = new HashMap<KeyHive, Key>();

			keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
			logger.info("Using keystore " + keystoreFile);
			loadMasterKeys(keystoreFile, keystorePassword, createFileIfNotExist);
		}
		catch (Exception e)
		{
			throw new CryptoException("Failed initializing keystore from file " + keystoreFile, e);
		}
	}

	

	/**
	 * loads the keys from file. if file does not exist, it creates it, 
	 * creates the keys and stores it. if file exists but one or more keys are missing - it creates the keys and
	 * stores to the file.
	 * @param fileName
	 * @param password
	 * @param createFileIfNotExist 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadMasterKeys(String fileName, String password,
			boolean createFileIfNotExist) 
					throws NoSuchAlgorithmException,
					KeyStoreException,
		            CertificateException,
		            FileNotFoundException,
		            IOException
	{
		boolean keystoreModified = false;
		KeyGenerator keyGen = null;
		try
		{
			keyStore.load(new FileInputStream(fileName), password.toCharArray());
		}
		catch (FileNotFoundException e)
		{
			logger.info("Keystore file does not exist; Will try to create a new one");
			keyStore.load(null, null);
		}

		// Load Symmetric keys
		for (KeyHive hive : KeyHive.values())
		{
			Key key = null;
			String keyAlias = "WatchDox_" + hive.toString();
			char[] keyPassword = (password + "__" + keyAlias).toCharArray();
			try
			{
				key = keyStore.getKey(keyAlias, keyPassword);
			}
			catch (UnrecoverableKeyException e)
			{
				 //This key does not exist; Will be created by the next if statement 
			}
			if (key == null)
			{
				if (keyGen == null)
				{
					keyGen = KeyGenerator.getInstance("AES");
					keyGen.init(SYMMETRIC_KEY_LENGTH);
				}
				logger.info("Creating NEW symmetric key: " + keyAlias);
				key = keyGen.generateKey();
				keyStore.setKeyEntry(keyAlias, key, keyPassword, null);
				keystoreModified = true;
			}
			else
			{
				logger.info("Loaded symmetric key: " + keyAlias);
			}
			keys.put(hive, key);
		}

		// Load asymmetric keys
		char[] keyPassword = (password + "__" + ASYMMETRIC_KEY_NAME).toCharArray();
		try
		{
			privateKey = (PrivateKey) keyStore.getKey(ASYMMETRIC_KEY_NAME, keyPassword);
			certificate = keyStore.getCertificate(ASYMMETRIC_KEY_NAME);
		}
		catch (UnrecoverableKeyException e)
		{
			privateKey = null;
		}

		if ((privateKey == null) || (certificate == null))
		{
			logger.error("no keys were found... ERROR " + ASYMMETRIC_KEY_NAME);
			logger.error("Creating NEW asymmetric keypair: " + ASYMMETRIC_KEY_NAME);
//			KeyWithCertificate keys = generateAndSaveAssymetricKeys(ASYMMETRIC_KEY_NAME, keyPassword);
//			privateKey = keys.getPrivateKey();
//			certificate = keys.getCertificate();
//			keystoreModified = true;
		}
		else
		{
			logger.info("Loaded asymmetric key-pair: " + ASYMMETRIC_KEY_NAME);
		}

		if (keystoreModified && createFileIfNotExist)
		{
			// We loaded some keys, we need to update the keystore
			keyStore.store(new FileOutputStream(fileName), password.toCharArray());
		}

	}


	public Key getKey(ImmutablePair<KeyHive, String> keyParams)
	{
		Key masterKey = keys.get(keyParams.getLeft());

		return masterKey;
	}

	
	public Cipher getCipher(Key key, int cryptMode) throws InvalidKeyException
	{
		Cipher cipher = null;
		try
		{
			if (key.getEncoded().length > 16)
			{
				cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			}
			else
			{
				cipher = Cipher.getInstance("AES");
			}
		}
		catch (GeneralSecurityException e)
		{
			throw new CryptoException("Cipher creation failed", e);
		}

		cipher.init(cryptMode, key);

		return cipher;
	}
}
