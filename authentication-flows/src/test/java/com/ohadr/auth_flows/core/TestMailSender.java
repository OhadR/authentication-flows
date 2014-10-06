package com.ohadr.auth_flows.core;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class TestMailSender
{
	@Test
	public void test() 
	{
		//TODO: in roder to test the email-sending, change to "...-real-email.xml" 
		//and make sure in client.properties you have user+password of valid email account.
		ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet-mock-email.xml");
	 
    	MailSender sender2 = (MailSender) context.getBean("mailSender");

	    	
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo("ohad.redlich@gmail.com");
		msg.setSubject("Testing Subject");
		msg.setText("Dear Mail Crawler, \n\n No spam to my email, please!");

		JavaMailSenderImpl impl = (JavaMailSenderImpl)sender2;
		System.out.println("sending... from: " + impl.getUsername());
		try
		{
			sender2.send(msg);
		}
		catch (MailException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail( e.getMessage() );
		}

        System.out.println("Done");
	}

}
