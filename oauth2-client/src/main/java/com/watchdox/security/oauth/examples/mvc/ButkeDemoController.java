package com.watchdox.security.oauth.examples.mvc;

import com.watchdox.security.oauth.examples.ButkeDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
@Controller
@RequestMapping("/hello")
public class ButkeDemoController
{

    @Autowired
	private ButkeDemoService butkeDemoService;

    @RequestMapping(method = RequestMethod.GET)
	public String demo(ModelMap model) throws Exception {
		model.addAttribute("message", butkeDemoService.getTrustedMessage());
		return "hello";
	}


	public void setButkeDemoService(ButkeDemoService butkeDemoService) {
		this.butkeDemoService = butkeDemoService;
	}

}
