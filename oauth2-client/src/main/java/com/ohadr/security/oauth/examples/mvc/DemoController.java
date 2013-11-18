package com.ohadr.security.oauth.examples.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ohadr.security.oauth.examples.DemoService;


/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
@Controller
@RequestMapping("/hello")
public class DemoController
{

    @Autowired
	private DemoService butkeDemoService;

    @RequestMapping(method = RequestMethod.GET)
	public String demo(ModelMap model) throws Exception {
		model.addAttribute("message", butkeDemoService.getTrustedMessage());
		return "hello";
	}


	public void setButkeDemoService(DemoService butkeDemoService) {
		this.butkeDemoService = butkeDemoService;
	}

}
