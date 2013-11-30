package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;

import com.ohadr.auth_flows.config.OAuthServerProperties;
import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.crypto.service.CryptoService;

@Controller
@RequestMapping(value = "/aa")
public class ActivateAccountEndpoint 
{
	@Autowired
	private OAuthServerProperties oAuthServerProperties;

	@Autowired
	private AuthenticationAccountRepository oAuthRepository;

	@Autowired
	private CryptoService	cryptoService;

	
	@RequestMapping
    public void activateAccount(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
		String redirectUri;

		EmailExtractedData extractedData = extractEmailData(request);
		
		
		
		if (!extractedData.expired)
		{
//TODO			PlatformTransactionManager transactionManager = (PlatformTransactionManager) ContextLoader.getCurrentWebApplicationContext()
//			                                                                                          .getBean("oAuthTransactionManager");
//
//			TransactionStatus oAuthTransaction = transactionManager.getTransaction(new DefaultTransactionAttribute());

			// enable the account
			oAuthRepository.setEnabled(extractedData.userEmail);
			// reset the #attempts, since there is a flow of exceeding attempts number, so when clicking the link
			// (in the email), we get here and enable the account and reset the attempts number
			oAuthRepository.setLoginSuccess(extractedData.userEmail);

//TODO			transactionManager.commit(oAuthTransaction);

			request.getSession().invalidate();
			request.getSession(true);
			SecurityContextHolder.getContext().setAuthentication(null);

//			if (redirectUri == null || redirectUri.isEmpty())
//			{
//				redirectUri = extractedData.redirectUri;
//
//				// fallback: (we might get here if login failed and account is locked and we have no redirect-uri:
//				if (redirectUri == null || redirectUri.isEmpty())
//				{
					redirectUri = request.getContextPath() + "/login/AccountActivated.htm";
//				}
//			}

			response.sendRedirect(redirectUri);
		}
	}

	
	
	private EmailExtractedData extractEmailData(HttpServletRequest request) 
	{
//		String encRedirectUri = FlowsUtil.getParamRedirectUri(request);
		String encUserAndTimestamp = FlowsUtil.getParamsUserAndTimestamp(request);
		
		
		EmailExtractedData extractedData = new EmailExtractedData();
		ImmutablePair<Date, String> stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		
		
		extractedData.userEmail = stringAndDate.getRight();
		extractedData.emailCreationDate = stringAndDate.getLeft();
//		extractedData.redirectUri = cryptoService.extractString(encRedirectUri);
		extractedData.expired = (System.currentTimeMillis() - extractedData.emailCreationDate.getTime() > 
			(oAuthServerProperties.getLinksExpirationMinutes() * 1000 * 60L));
		return extractedData;
	}
	
	private class EmailExtractedData
	{
		String redirectUri;
		String userEmail;
		Date emailCreationDate;
		boolean expired;
	}
}
