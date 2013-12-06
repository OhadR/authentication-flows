package com.ohadr.auth_flows.mocks;

import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailSenderMock implements MailSender
{
	private static Logger log = Logger.getLogger(MailSenderMock.class);

	@Override
	public void send(SimpleMailMessage msg) throws MailException
	{
		log.info("MOCK: sending mail to " + msg.getTo() + ", subject: " + msg.getSubject());
		log.info("MOCK: mail body: " + msg.getText());
	}

	@Override
	public void send(SimpleMailMessage[] simpleMessages) throws MailException
	{
		// TODO Auto-generated method stub
		
	}

}
