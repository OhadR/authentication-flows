package com.watchdox.butke.token;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestTokenGenerator 
{
	SignedTokenGenerator stg = new SignedTokenGenerator();


	public SignedTokenGenerator getSignedTokenGenerator() 
	{
		return stg;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("spring-servlet.xml");

        SignedTokenGenerator signedTokenGenerator = appContext.getBean(SignedTokenGenerator.class);

        
		try 
		{
			String token = signedTokenGenerator.generateToken("uri@watchdox.com",
					"com.watchdox.shalom", //issuer
					null,	//deviceName
					null,	//clientPublicKey
					100		//secondsToExpire
					);
			
			System.out.println("the token: " + token);
		} 
		catch (InvalidKeyException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	

}
