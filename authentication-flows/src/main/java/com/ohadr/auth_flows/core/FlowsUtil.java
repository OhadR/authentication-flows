package com.ohadr.auth_flows.core;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class FlowsUtil 
{

	
	// some mail clients add the "amp;" after the '&' sign that defines a parameter.
	// so the parameter "c" may be called "amp;c" if the customer has such a browser
	private static String getParamX(HttpServletRequest request, String x)
	{
		String result = request.getParameter(x);

		// Maybe it's amp problem?
		if (result == null)
		{
			result = request.getParameter("amp;" + x);
		}

		return result;
	}


	// we identify the redirect-URI by the 'ru' char:
	public static String getParamRedirectUri(HttpServletRequest request)
	{
		return getParamX(request, "ru");
	}

	// we identify the timestamp by the 'ts' char:
	public static String getParamTimestamp(HttpServletRequest request)
	{
		return getParamX(request, "ts");
	}

	// user and timestamp come along with 'uts' char:
	public static String getParamsUserAndTimestamp(HttpServletRequest request)
	{
		return getParamX(request, "uts");
	}
	
	public static void logStackTrace(Logger log) 
	{
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) 
		{
			sb.append(ste).append("\n");
		}
		log.debug(sb);
	}


	public static String getServerPath(HttpServletRequest request)
	{
		String path = null;
		String contextPath = request.getServletPath();					//e.g. /createAccount
		String requestURL = request.getRequestURL().toString();			//e.g. https://ohadr.com:8443/client/createAccount
		int indexOf = StringUtils.indexOf(requestURL, contextPath);
		if(indexOf != -1)
		{
			path = StringUtils.substring(requestURL, 0, indexOf);
		}
		return path;
	}

	
	public static String unescapeJaveAndEscapeHtml(String input)
	{
		String tmp = StringEscapeUtils.unescapeJava( input );
		return StringEscapeUtils.escapeHtml( tmp );
	}

}
