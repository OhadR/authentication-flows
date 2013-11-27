package com.ohadr.auth_flows.core;



import org.junit.Test;

public class TestMailSender
{

	@Test
	public void test() 
	{
		MailSenderImpl sender = new MailSenderImpl();


		sender.sendMail(
				"ohad.redlich@gmail.com",
				"Testing Subject", 
				"Dear Mail Crawler, \n\n No spam to my email, please!"
				);

        System.out.println("Done");
	}

}
