package com.ohadr.auth_flows.web;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;

import com.ohadr.auth_flows.config.AuthFlowsProperties;
import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.crypto.exception.CryptoException;
import com.ohadr.crypto.service.CryptoService;

public abstract class FlowsEndpointsCommon
{
	@Autowired
	private AuthFlowsProperties oAuthServerProperties;

	@Autowired
	private CryptoService	cryptoService;

	@Autowired
	protected AuthenticationFlowsProcessor processor;

	

	
	protected EmailExtractedData extractEmailData(HttpServletRequest request) throws CryptoException 
	{
//		String encRedirectUri = FlowsUtil.getParamRedirectUri(request);
		String encUserAndTimestamp = FlowsUtil.getParamsUserAndTimestamp(request);
		
		
		EmailExtractedData extractedData = new EmailExtractedData();
		ImmutablePair<Date, String> stringAndDate;
		
		//issue #9: exception on console, if restore-pass link was tampered with. handle here the exception
		stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		
		extractedData.userEmail = stringAndDate.getRight();
		extractedData.emailCreationDate = stringAndDate.getLeft();
//		extractedData.redirectUri = cryptoService.extractString(encRedirectUri);
		extractedData.expired = (System.currentTimeMillis() - extractedData.emailCreationDate.getTime() > 
			(oAuthServerProperties.getLinksExpirationMinutes() * 1000 * 60L));
		return extractedData;
	}

	
	class EmailExtractedData
	{
		
	String redirectUri;
	String userEmail;
	Date emailCreationDate;
	boolean expired;
	}
}