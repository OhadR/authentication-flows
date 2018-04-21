package com.ohadr.authentication.token;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ohadr.crypto.service.CryptoService;



@Component
public class SignedTokenGenerator
{
	@Autowired
	private CryptoService cryptoService;

	private String encodeQueryParams(Map<String, String> parameters) throws UnsupportedEncodingException
	{
		String result = "";
		for (String paramName : parameters.keySet())
		{
			result += URLEncoder.encode(paramName, "UTF-8");
			result += "=" + URLEncoder.encode(parameters.get(paramName), "UTF-8") + "&";
		}
		return result;
	}

	private String createSignature(String data)
	{
		String encoded = cryptoService.generateEncodedString(data);
		return encoded;
	}

	public String generateToken(String userEmail, String issuer, String deviceName, String clientPublicKey,
	                            int secondsToExpire) throws UnsupportedEncodingException
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("user", userEmail);
		parameters.put("issuer", issuer);
		parameters.put("expires", String.valueOf(new Date().getTime() / 1000 + secondsToExpire));
		if (deviceName != null)
		{
			parameters.put("device", deviceName);
		}
		if (clientPublicKey != null)
		{
			parameters.put("pubkey", clientPublicKey);
		}
		String data = encodeQueryParams(parameters);
		String signature = createSignature(data);
		return data + ":" + signature;
	}
}
