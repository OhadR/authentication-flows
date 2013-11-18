package com.ohadr.oauth_srv.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ohadr.authentication.token.interfaces.KeystoreService;


@Controller
public class PublicKeyController
{
	@Autowired
	private KeystoreService keystoreService;

	@RequestMapping("/publicKey")
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType("text/plain");
		String encodedKey = Base64.encodeBase64String(keystoreService.getPublicKey().getEncoded());
		response.getWriter().append(encodedKey);
		return null;
	}

	public void setKeystoreService(KeystoreService keystoreService)
	{
		this.keystoreService = keystoreService;
	}
}
