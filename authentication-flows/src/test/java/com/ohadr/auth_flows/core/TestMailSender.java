package com.ohadr.auth_flows.core;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class TestMailSender
{
	@Test
	public void test() 
	{
		ApplicationContext context = 
	             new ClassPathXmlApplicationContext("spring-servlet-real-email.xml");
	 
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
		catch (MailAuthenticationException authEx)
		{
			authEx.printStackTrace();
			System.out.println("failed to send email due to authentication issue; " + authEx.getMessage());
		}
		catch (MailException e)
		{
			e.printStackTrace();
			System.out.println("failed to send email; " + e.getMessage());
//			Assert.fail( e.getMessage() );
		}

        System.out.println("Done");
	}

}
