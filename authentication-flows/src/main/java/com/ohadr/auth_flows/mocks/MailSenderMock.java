package com.ohadr.auth_flows.mocks;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.ohadr.auth_flows.interfaces.MailSender;


@Service
public class MailSenderMock implements MailSender
{
	private static Logger log = Logger.getLogger(MailSenderMock.class);

	@Override
	public void sendMail(String adressee, String subject, String mailBody) 
	{
		log.info("MOCK: sending mail to " + adressee + ", subject: " + subject);
	}

}
