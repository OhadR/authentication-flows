package com.ohadr.crypto.service;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.interfaces.ICryptoUtil;

@Component
public class CryptoService
{
	private static final String URL_CONTENT = "UrlContent";
	private static final String ENCRYPTION_VERSION = "a";
	
	@Autowired
	private ICryptoUtil cryptoUtil;

	
	public String createEncodedBased64String(byte[] content)
	{
		String based64EncryptedContent = cryptoUtil.encryptAndBase64(content, getKey());
		return ENCRYPTION_VERSION + based64EncryptedContent.replaceAll("=", "").replaceAll("\\+", ".");
	}

	
	private Key getKey()
	{
		return cryptoUtil.getCryptoKey(URL_CONTENT);
	}

	
	/**************************************************************************/
	public String generateEncodedString(String redirectUri)
	{
		return createEncodedContent(redirectUri);
	}
	
	public String extractString(String based64EncryptedContent)
	{
		byte[] content = getDecodedStringFromEncodedBased64String(based64EncryptedContent);
		String redirectUri = extractString(content, 0, content.length);		
		return redirectUri;
	}


	/**************************************************************************/
	public String generateEncodedInt(int value)
	{
		return createEncodedContent(value);
	}

	public int extractInt(String based64EncryptedContent)
	{
		byte[] content = getDecodedStringFromEncodedBased64String(based64EncryptedContent);
		int value = extractInteger(content, 0, 4);		
		return value;
	}
	
	/**************************************************************************/
	public String generateEncodedDate(long millis)
	{
		int time = (int) (millis / 1000);
		return generateEncodedInt(time);
	}

	public String generateEncodedDate(Date date)
	{
		long time = date.getTime();
		return generateEncodedDate(time);
	}
	
	public Date extractDate(String based64EncryptedContent)
	{
		long timestamp = extractInt(based64EncryptedContent);
		Date date = new Date(timestamp * 1000);
		return date;
	}
	/**************************************************************************/

	
	
	public ImmutablePair<Date ,String> extractStringAndDate(String based64EncryptedContent)
	{
		byte[] content = getDecodedStringFromEncodedBased64String(based64EncryptedContent);
		Date date = extractDate(based64EncryptedContent);		
		String str = extractString(content, 4, content.length);
		ImmutablePair<Date ,String> stringAndDate = new ImmutablePair<Date ,String>(date, str);
		
		return stringAndDate;
	}
	
	
	/**************************************************************************/
	/**
	 * Encode Integers, Strings and byte into encrypted based-64 string
	 * 
	 * @param params
	 * @return encoded content (Base64)
	 */
	public String createEncodedContent(Object... params)
	{
		byte[] content = serializeToByteArray(params);
		return createEncodedBased64String(content);
	}

	public byte[] serializeToByteArray(Object... params)
	{
		int contentSize = 0;
		for (Object obj : params)
		{
			if (obj != null && (obj instanceof Integer || obj instanceof Date))
			{
				contentSize += 4;
			}
			else if (obj != null && obj instanceof String)
			{
				contentSize += ((String) obj).getBytes().length;
			}
			else if (obj != null && obj instanceof Byte)
			{
				contentSize++;
			}
			else if (obj != null && obj instanceof byte[])
			{
				contentSize += ((byte[]) obj).length;
			}
		}

		byte[] content = new byte[contentSize];

		int currentIndex = 0;
		for (Object obj : params)
		{
			if (obj != null && obj instanceof Integer)
			{
				storeInteger(content, (Integer) obj, currentIndex);
				currentIndex += 4;
			}
			if (obj != null && obj instanceof Date)		//date is unique coz we take its long, devide by 1000 (secs, not millisecs)
			{
				Date temp = (Date)obj;
				int timeAsLong = (int)(temp.getTime() / 1000);
				storeInteger(content, timeAsLong, currentIndex);
				currentIndex += 4;
			}
			else if (obj != null && obj instanceof String)
			{
				currentIndex += storeString(content, (String) obj, currentIndex);
				// currentIndex += ((String)obj).length();
			}
			else if (obj != null && obj instanceof Byte)
			{
				content[currentIndex] = (Byte) obj;
				currentIndex++;
			}
			else if (obj != null && obj instanceof byte[])
			{
				int length = ((byte[]) obj).length;
				System.arraycopy(obj, 0, content, currentIndex, length);
				currentIndex += length;
			}

		}
		return content;
	}
	
	private static void storeInteger(byte[] content, int theInteger, int from)
	{
		byte[] intBytes = intToByteArray(theInteger);
		for (int i = 0; i < 4; ++i)
		{
			content[from + i] = intBytes[i];
		}
	}

	private static int storeString(byte[] content, String string, int from)
	{
		byte[] stringBytes = string.getBytes();
		for (int i = 0; i < stringBytes.length; ++i)
		{
			content[from + i] = stringBytes[i];
		}
		return stringBytes.length;
	}

	/**************************************************************************/

	/**************************************************************************/
	private static byte[] intToByteArray(int value)
	{
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++)
		{
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	private static int byteArrayToInt(byte[] value)
	{
		int i = 0;
		i |= value[0] & 0xFF;
		i <<= 8;
		i |= value[1] & 0xFF;
		i <<= 8;
		i |= value[2] & 0xFF;
		i <<= 8;
		i |= value[3] & 0xFF;

		return i;
	}
	/**************************************************************************/

	
	
	
	private byte[] getDecodedStringFromEncodedBased64String(String based64EncryptedContent)
	{
		// Step 1: Cut version character
		String contentWithSuffix = based64EncryptedContent.substring(1);

		// Step 2: Add padding as needed
		if (contentWithSuffix.endsWith("-"))
		{
			// Supporting older URLS with "-" as suffix instead of "="
			contentWithSuffix = contentWithSuffix.replaceAll("\\-", "=");
		}
		else
		{
			switch (contentWithSuffix.length() % 4)
			{
			case 2:
				contentWithSuffix += "==";
				break;

			case 3:
				contentWithSuffix += "=";
				break;
			}
		}

		// Step 3: Decrypt
		byte[] content;
		try
		{
			content = cryptoUtil.decryptBase64(contentWithSuffix.replaceAll("\\.", "+"), getKey());
		}
		catch (GeneralSecurityException e)
		{
			throw new CryptoException("Failed to decrypt URL content " + based64EncryptedContent, e);
		}
		return content;
	}
	
	public int extractInteger(byte[] content, int from, int to)
	{
		byte[] bytesOfInt = getPartialByteArray(content, from, to);

		return byteArrayToInt(bytesOfInt);
	}

	public String extractString(byte[] content, int from, int to)
	{
		byte[] bytesOfString = getPartialByteArray(content, from, to);

		return new String(bytesOfString);
	}

	private static byte[] getPartialByteArray(byte[] content, int from, int to)
	{
		byte[] bytesOfString = new byte[to - from];
		for (int i = 0; i < bytesOfString.length; ++i)
		{
			bytesOfString[i] = content[from + i];
		}
		return bytesOfString;
	}


}
