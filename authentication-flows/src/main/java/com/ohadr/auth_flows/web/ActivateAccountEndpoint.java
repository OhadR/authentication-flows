package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.ohadr.auth_flows.interfaces.AuthenticationFlowsProcessor;
import com.ohadr.auth_flows.types.FlowsConstatns;
import com.ohadr.auth_flows.web.FlowsEndpointsCommon.EmailExtractedData;
import com.ohadr.crypto.exception.CryptoException;

@Controller
@RequestMapping(value = "/aa")
public class ActivateAccountEndpoint extends FlowsEndpointsCommon 
{
	
	@RequestMapping
    public View activateAccount(HttpServletRequest request) throws IOException 
    {
		RedirectView rv = new RedirectView();
		Map<String, String> attributes = new HashMap<String, String>();

		EmailExtractedData extractedData= null;

		String redirectUri;

		try
		{
			extractedData = extractEmailData(request);
		} 
		catch (CryptoException cryptoEx)
		{
//			log.error("Could not extract data from URL", cryptoEx);
			cryptoEx.printStackTrace();
			
			attributes.put(FlowsConstatns.ERR_HEADER,  "URL IS INVALID");		
			attributes.put(FlowsConstatns.ERR_MSG,  "URL IS INVALID" + " exception message: " + cryptoEx.getMessage());		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "error.jsp");
			return rv;
		}
		
		
		if(extractedData.expired)
		{
//			log.error("user " + extractedData.userEmail + " tried to use an expired link");

			attributes.put(FlowsConstatns.ERR_HEADER,  "URL IS EXPIRED");		
			attributes.put(FlowsConstatns.ERR_MSG,  "URL IS Expired");		
			//adding attributes to the redirect return value:
			rv.setAttributesMap(attributes);
			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "error.jsp");
		}
		else		
		{
			// enable the account
			processor.setEnabled(extractedData.userEmail);

			// reset the #attempts, since there is a flow of exceeding attempts number, so when clicking the link
			// (in the email), we get here and enable the account and reset the attempts number
			processor.setLoginSuccessForUser(extractedData.userEmail);

			request.getSession().invalidate();
			request.getSession(true);
			SecurityContextHolder.getContext().setAuthentication(null);


			rv.setUrl(FlowsConstatns.LOGIN_FORMS_DIR +"/" + "AccountActivated.htm");
		}
		return rv;
	}
}
