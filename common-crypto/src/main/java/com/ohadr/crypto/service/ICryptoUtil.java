package com.ohadr.crypto.service;
 

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.DecoderException;

import com.watchdox.security.crypto.KeyHive;

public interface ICryptoUtil
{
	/* Symmetric */
	public InputStream decrpytStream(InputStream sourceStream, Key key);

	public InputStream encryptStream(InputStream sourceStream, Key key);

	public OutputStream decryptStream(OutputStream sourceStream, Key key);

	public OutputStream encryptStream(OutputStream sourceStream, Key key);

	public String encryptAndBase64(byte[] data, Key key);

	public byte[] decryptBase64(String base64andEncrypted, Key key) throws IllegalBlockSizeException,
	                                                               BadPaddingException;

	public byte[] getSealerKey(String documentUuid);

	/* Asymmetric */
	public byte[] signApiToken(byte[] data);

	public boolean validateApiTokenSignature(byte[] data, byte[] signature);

	public boolean validateApiTokenSignature(PublicKey publicKeys, byte[] data, byte[] signature);
	
	/* */
	public Key getCryptoKey(KeyHive hive, String seed);

	public byte[] encryptBytes(byte[] data, Key cryptoKey);

	public byte[] decryptBytes(byte[] data, Key cryptoKey) throws IllegalBlockSizeException, BadPaddingException;

	public char[] encryptBytesToHex(byte[] data, Key cryptoKey);

	public byte[] decryptBytesFromHex(char[] data, Key cryptoKey) throws IllegalBlockSizeException, BadPaddingException, DecoderException;

	public byte[] convertDsaSignatureToDerEncoding(byte[] dsaSignature);

	public boolean validateSignature(PublicKey key, 
			String signature,
			String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;

}
