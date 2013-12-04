package com.ohadr.auth_flows.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ohadr.auth_flows.core.FlowsUtil;
import com.ohadr.auth_flows.interfaces.AuthenticationAccountRepository;
import com.ohadr.auth_flows.types.FlowsConstatns;

/**
 * user clicks on the link in the "forgot password" email, and gets here.
 * @author OhadR
 *
 */
@Controller
@RequestMapping(value = "/rp")
public class RestorePasswordEndpoint extends FlowsEndpointsCommon
{
	private static Logger log = Logger.getLogger(RestorePasswordEndpoint.class);

	@Autowired
	private AuthenticationAccountRepository repository;

	
	@RequestMapping
	public void restorePassword(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		EmailExtractedData extractedData = extractEmailData(request);

		if(extractedData.expired)
		{
			log.error("user " + extractedData.userEmail + " tried to use an expired link");
			String redirectUri = request.getContextPath() + "/login/index.htm";		//psns=password not changed
			response.sendRedirect( redirectUri );
			return;

		}
		else
		{
			//we send also the signed-email, so no one can change the email and set-new-password for another user:
			String encodedEmailAndTimestamp = FlowsUtil.getParamsUserAndTimestamp(request);

			Date lastChange = repository.getPasswordLastChangeDate(extractedData.userEmail);

			Date emailCreationDate = extractedData.emailCreationDate;


			request.getSession().invalidate();
			request.getSession(true);
			SecurityContextHolder.getContext().setAuthentication(null);


			String redirectUri = extractedData.redirectUri;

			//if password was changed AFTER the email creation (that is AFTER the user initiated "4got password" flow) - 
			//it means the request is irrelevant
			if(lastChange.after(emailCreationDate))
			{
				log.error("user " + extractedData.userEmail + " tried to use an expired link: password was already changed AFTER the timestamp of the link");
//TODO
				redirectUri = request.getContextPath() + "/login/index.htm"; 		//psns=password not changed 
				response.sendRedirect( redirectUri );
				return;
			}


			//		String encoded = URLEncoder.encode(redirectUri, "utf-8");		//returns something like: https%3A%2F%2Foauthsubdomain.ohad.sealdoc
//			String escaped = StringEscapeUtils.escapeHtml4( redirectUri );


			
			//after all the checks, all look good (link not expired, etc). so show the user the "set new password" page.
			//if "secret question" is implemented, here you get the secret Q and show the user the screen to answer it. then
			//check the answer, etc.  

			redirectUri = request.getContextPath() + "/login/setNewPassword.htm"
					+ "?"
					+ FlowsConstatns.HASH_PARAM_NAME 
					+ "=" + encodedEmailAndTimestamp;

			response.sendRedirect( redirectUri );
		}
	}
}
