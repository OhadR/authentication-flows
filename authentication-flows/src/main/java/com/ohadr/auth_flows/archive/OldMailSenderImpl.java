package com.ohadr.auth_flows.archive;


import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


/**
 * this implementation uses pure javax.mail to send mail.
 * Deprecated because newer version uses Spring @link:JavaMailSenderImpl
 * @author OhadR
 *
 */
@Deprecated
public class OldMailSenderImpl implements MailSender 
{
	private Session session;
	

	public OldMailSenderImpl()
	{
		final String username = "bmc.incubator@gmail.com";
		final String password = "**thepassword**";

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");
		//    	props.put("mail.smtp.port", "465");

		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() 
			{
				return new PasswordAuthentication(username, password);
			}
		});

	}


	@Override
	public void send(SimpleMailMessage msg) throws MailException
	{
		try 
		{
			Message message = new MimeMessage(session);
			//      message.setFrom(new InternetAddress("your_user_name@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, 
					InternetAddress.parse( msg.getTo().toString() ));
			message.setSubject( msg.getSubject() );
			message.setText( msg.getText() );

			Transport.send(message);
		} 
		catch (MessagingException e) 
		{
			throw new RuntimeException(e);
		}    	
	}

	@Override
	public void send(SimpleMailMessage[] simpleMessages) throws MailException
	{
		// TODO Auto-generated method stub

	}

}