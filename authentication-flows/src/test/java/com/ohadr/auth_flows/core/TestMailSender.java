package com.ohadr.auth_flows.core;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class TestMailSender
{
	@Test
	public void test() 
	{
	   	ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet.xml");
	 
    	MailSender sender2 = (MailSender) context.getBean("mailSender");

	    	
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo("ohad.redlich@gmail.com");
		msg.setSubject("Testing Subject");
		msg.setText("Dear Mail Crawler, \n\n No spam to my email, please!");

		try
		{
			sender2.send(msg);
		}
		catch (MailException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Done");
	}

}
