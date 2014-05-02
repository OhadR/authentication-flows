package com.ohadr.auth_flows.core.gae;


import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


/**
 * this implementation uses pure javax.mail to send mail.
 * Deprecated because newer version uses Spring @link:JavaMailSenderImpl
 * @author OhadR
 *
 */

public class GAEMailSenderImpl implements MailSender 
{
	private static Logger log = Logger.getLogger(GAEMailSenderImpl.class);

    private Session session;
	

	public GAEMailSenderImpl()
	{
		final String username = "bmc.incubator@gmail.com";
		final String password = "theheatison";

		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");
		//    	props.put("mail.smtp.port", "465");

//		session = Session.getDefaultInstance(props, null);
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
		log.info(msg.toString());
		
		try 
		{
		    Message message = new MimeMessage(session);
		    message.setFrom(new InternetAddress("ohadr.developer@gmail.com", "ohadr.com Admin"));
		    message.addRecipient(Message.RecipientType.TO,
		    	     new InternetAddress( msg.getTo()[0] ));		//Spring's getTo returns String[]

/*		    message.setRecipients(Message.RecipientType.TO, 
					InternetAddress.parse( msg.getTo().toString() ));
			
*/		    message.setSubject( msg.getSubject() );
			message.setText( msg.getText() );

			Transport.send(message);
		} 
		catch (MessagingException e) 
		{
			log.error("MessagingException: ", e);
			throw new RuntimeException(e);
		} 
		catch (UnsupportedEncodingException e) 
		{
			log.error("UnsupportedEncodingException: ", e);
			e.printStackTrace();
		}    	
	}

	@Override
	public void send(SimpleMailMessage[] simpleMessages) throws MailException
	{
		// TODO Auto-generated method stub

	}

}