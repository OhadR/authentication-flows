package com.ohadr.oauth_srv.core;


import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


//TODO: consider using Spring' JavaMailSenderImpl
@Service
public class MailSender 
{
	private Session session;
	
	
    public MailSender()
    {
        final String username = "ohadr@*********.com";
        final String password = "*******";

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getInstance(props,
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() 
            {
                return new PasswordAuthentication(username, password);
            }
          });
    	
    }
	
	public void sendMail(
    		String adressee,
    		String subject,
    		String mailBody,
    		String action)
    {
        try 
        {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("your_user_name@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse( adressee ));
            message.setSubject( subject );
            message.setText( mailBody );

            Transport.send(message);
        } 
        catch (MessagingException e) 
        {
            throw new RuntimeException(e);
        }    	
    }

	public static void main(String[] args)
    {
		MailSender sender = new MailSender();


		sender.sendMail(
				"ohad.redlich@gmail.com",
				"Testing Subject", 
				"Dear Mail Crawler, \n\n No spam to my email, please!",
				null
				);

        System.out.println("Done");
    }
}