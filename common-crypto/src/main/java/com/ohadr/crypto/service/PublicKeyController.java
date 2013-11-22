package com.ohadr.crypto.service;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ohadr.crypto.interfaces.KeystoreService;



@Controller
public class PublicKeyController
{
	@Autowired
	private KeystoreService keystoreService;

	@RequestMapping("/publicKey")
	protected /*ModelAndView */ void getPublicKey(HttpServletResponse response) throws Exception
	{
		response.setContentType("text/plain");
		String encodedKey = Base64.encodeBase64String(keystoreService.getPublicKey().getEncoded());
		response.getWriter().append(encodedKey);
	}
}
