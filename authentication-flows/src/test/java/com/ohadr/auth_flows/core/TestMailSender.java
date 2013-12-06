package com.ohadr.auth_flows.core;



import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class TestMailSender
{

	@Test
	public void test() 
	{
		JavaMailSenderImpl sender = new JavaMailSenderImpl();

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo("ohad.redlich@gmail.com");
		msg.setSubject("Testing Subject");
		msg.setText("Dear Mail Crawler, \n\n No spam to my email, please!");

		sender.send( msg );

        System.out.println("Done");
	}

}
