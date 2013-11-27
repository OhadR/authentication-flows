package com.ohadr.auth_flows.interfaces;

/**
 * DEPRECATED: consider using Spring' MailSender Iface
 * @author OhadR
 *
 */
@Deprecated
public interface MailSender
{
	void sendMail(
    		String adressee,
    		String subject,
    		String mailBody);

}
