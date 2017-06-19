package com.ohadr.crypto.interfaces;
 

import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


public interface ICryptoUtil
{
	String encryptAndBase64(byte[] data, Key key);

	byte[] decryptBase64(String base64andEncrypted, Key key) 
			throws IllegalBlockSizeException, BadPaddingException;

	Key getCryptoKey(String seed);
}
