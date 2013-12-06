package com.ohadr.auth_flows.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * user clicks on the link in the "forgot password" email, and gets here.
 * @author OhadR
 *
 */
@Controller
@RequestMapping(value = "/cp")
public class ChangePasswordEndpoint
{
	
	@RequestMapping
	public void changePassword(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String redirectUri = request.getContextPath() + "/login/changePassword.htm";

		response.sendRedirect( redirectUri );
	}
}
