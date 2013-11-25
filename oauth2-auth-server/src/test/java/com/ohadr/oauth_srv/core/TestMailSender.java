package com.ohadr.oauth_srv.core;


import org.junit.Test;

public class TestMailSender
{

	@Test
	public void test() 
	{
		MailSender sender = new MailSender();


		sender.sendMail(
				"ohad.redlich@gmail.com",
				"Testing Subject", 
				"Dear Mail Crawler, \n\n No spam to my email, please!"
				);

        System.out.println("Done");
	}

}
