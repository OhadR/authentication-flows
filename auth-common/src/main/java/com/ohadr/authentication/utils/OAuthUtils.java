package com.ohadr.authentication.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public abstract class OAuthUtils
{
    private static final Logger log = Logger.getLogger(OAuthUtils.class);


	/**
	 * the SDK does not know how to encode params in the URL (only JSONs in the body...)
	 * so this util does the work
	 * @param param
	 * @return
	 */
	public static String encodeParam(String param)
	{
		String encodedParam = null;
		try
		{
			encodedParam = URLEncoder.encode( param, "utf8" );
		}
		catch (UnsupportedEncodingException e)
		{
			String errorText = e.getMessage();
			log.error(errorText);
		}
		return encodedParam;
	}	

}
