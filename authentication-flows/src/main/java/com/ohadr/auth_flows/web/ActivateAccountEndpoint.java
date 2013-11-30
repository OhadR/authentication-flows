package com.ohadr.auth_flows.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.crypto.service.CryptoService;

@Controller
@RequestMapping(value = "/aa")
public class ActivateAccountEndpoint 
{
	@Autowired
	private AuthenticationAccountRepository oAuthRepository;

	@Autowired
	private CryptoService	cryptoService;

	
	@RequestMapping
    public void getAccessToken(HttpServletRequest request, HttpServletResponse response) 
    {
		String redirectUri;

		EmailExtractedData extractedData = extractEmailData(request);
		
		
		
		if (!extractedData.expired)
		{
			PlatformTransactionManager transactionManager = (PlatformTransactionManager) ContextLoader.getCurrentWebApplicationContext()
			                                                                                          .getBean("oAuthTransactionManager");

			TransactionStatus oAuthTransaction = transactionManager.getTransaction(new DefaultTransactionAttribute());

			// enable the account
			oAuthRepository.setEnabled(extractedData.userEmail);
			// reset the #attempts, since there is a flow of exceeding attempts number, so when clicking the link
			// (in the email), we get here and enable the account and reset the attempts number
			oAuthRepository.setLoginSuccess(extractedData.userEmail);

			transactionManager.commit(oAuthTransaction);

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
					redirectUri = request.getContextPath() + OAUTH_WEB_APP_NAME + "/login/index.htm?dt=ua" 		//ua : unlock account
							;
//				}
//			}

			response.sendRedirect(redirectUri);
		}
	}

	
	
	private EmailExtractedData extractEmailData(HttpServletRequest request) 
	{
		String encRedirectUri = ManagedBeansUtil.getParamRedirectUri(request);
		String encUserAndTimestamp = ManagedBeansUtil.getParamsUserAndTimestamp(request);
		
		
		EmailExtractedData extractedData = new EmailExtractedData();
		ImmutablePair<Date, String> stringAndDate = cryptoService.extractStringAndDate(encUserAndTimestamp);
		
		
		extractedData.userEmail = stringAndDate.getRight();
		extractedData.emailCreationDate = stringAndDate.getLeft();
		extractedData.redirectUri = cryptoService.extractString(encRedirectUri);
		extractedData.expired = (System.currentTimeMillis() - extractedData.emailCreationDate.getTime() > AuthenticationUtil.EXPIRY_TIME);
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
